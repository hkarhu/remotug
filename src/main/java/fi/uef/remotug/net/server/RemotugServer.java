package fi.uef.remotug.net.server;

import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.EndPacket;
import fi.uef.remotug.net.ReadyPacket;
import fi.uef.remotug.net.StartPacket;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class RemotugServer {

	public static final int NO_ACTIVE_MATCH = -1;
	public static final int MATCH_STARTING = -2;
	public static final int MATCH_LENGTH = 30000;
	public static final int MATCH_START_DELAY = 5000;
	private final ConcurrentHashMap<Player, Channel> playerToChannelMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Channel, Player> channelToPlayerMap = new ConcurrentHashMap<>();
	
	private int playerIDs = 1;
	private long matchStarted = NO_ACTIVE_MATCH;
	
	private final ChannelGroup allClients;
	private final EventLoopGroup acceptEventLoopGroup;
	private final EventLoopGroup workerEventLoopGroup;
	private final List<ServersideHandler> serversideHandlers;
	
	public RemotugServer(String addr, int port) throws RuntimeException {
		acceptEventLoopGroup = new NioEventLoopGroup();
		workerEventLoopGroup = new NioEventLoopGroup();
		allClients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

		serversideHandlers = new ArrayList<ServersideHandler>();
		// --
		
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(acceptEventLoopGroup, workerEventLoopGroup)
				.channel(NioServerSocketChannel.class)
//				.handler(new LoggingHandler(LogLevel.TRACE))
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ServersideHandler serversideHandler = new ServersideHandler(allClients, RemotugServer.this);
						serversideHandlers.add(serversideHandler);
						ch.pipeline()
	            			.addLast(new LoggingHandler(LogLevel.DEBUG))
	            			.addLast(new ObjectEncoder())
							.addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)))
							.addLast(serversideHandler)
							;
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

		ChannelFuture f;
		RuntimeException ex = null;
		try {
			f = bootstrap.bind(addr, port).sync();
			
			if (!f.isSuccess()) {
				ex = new RuntimeException(f.cause());
			}
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				  public void run() {
					  
					  calculateAndSendBalances();
					  
					  long matchStarted = 0;
					  if((matchStarted = matchStarted()) != NO_ACTIVE_MATCH && matchStarted != MATCH_STARTING) {
						  if(System.currentTimeMillis() - matchStarted >= MATCH_LENGTH) {
							  endActiveMatch();
						  }
					  }
				  }
				}, 1000, 100);
			
		} catch (InterruptedException e) {
			ex = new RuntimeException(e);
			
		} finally {
			if (ex != null) {
				shutdown();
				throw ex;
			}
		}
	}

	public void shutdown() {
		acceptEventLoopGroup.shutdownGracefully();
		workerEventLoopGroup.shutdownGracefully();
	}
	
	public void playerReady(Channel channel) {
		if(this.matchStarted == NO_ACTIVE_MATCH) {
			Player player = this.channelToPlayerMap.get(channel);
			player.setReadyForMatch(true);
			
			int readyPlayers = 0;
			for(Player p: this.channelToPlayerMap.values()) {
				if(p.isReadyForMatch()) readyPlayers++;
			}
			
			this.allClients.writeAndFlush(new ReadyPacket(player.getId(), player.getName()));
			
			// && readyPlayers == this.channelToPlayerMap.size()
			if(readyPlayers == 2) {
				System.out.println("[server] starting a new match in 5 seconds");
				this.matchStarted = MATCH_STARTING;
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					  public void run() {
						  startMatch();
					  }
					}, MATCH_START_DELAY);
			} else {
				System.out.println("[server] " + readyPlayers + "/" + this.channelToPlayerMap.size() + " players are ready");
			}
		}
	}
	
	private void startMatch() {
		System.out.println("[server] match started");
		for(Player p: this.channelToPlayerMap.values()) {
			p.resetRopePos();
		}
		this.matchStarted = System.currentTimeMillis();
		this.allClients.writeAndFlush(new StartPacket(this.matchStarted, this.MATCH_LENGTH, this.MATCH_START_DELAY));
	}
	
	public void endActiveMatch() {
		System.out.println("[server] match ended");
		determineAndAnnounceWinner();
		
		for(Player p: this.channelToPlayerMap.values()) {
			p.setReadyForMatch(false);
		}
		
		this.matchStarted = NO_ACTIVE_MATCH;
	}
	
	public long matchStarted() {
		return this.matchStarted;
	}
	
	private void determineAndAnnounceWinner() {
		float bestRopePos = -1;
		Player winner = null;
		for(Player p: this.channelToPlayerMap.values()) {
			if(p.getRopePos() > bestRopePos) {
				bestRopePos = p.getRopePos();
				winner = p;
			}
		}
		System.out.println("[server] match winner > " + winner.getName() + ", " + winner.getId());
		
		this.allClients.writeAndFlush(new EndPacket(winner.getId()));
	}
	
	public void calculateAndSendBalances() {
		HashMap<Integer, Float> balances = new HashMap<Integer, Float>();
		for(Player p: channelToPlayerMap.values()) {
			if(p.getId() > 0) {
				balances.put(p.getId(), p.getBufferedKg());
				calculateBalanceForPlayer(p);
				//balances.put(p.getId(), calculateBalanceForPlayer(p));7
			}
		}
		
		for(Player p: channelToPlayerMap.values()) {
			float ropepos = calculateRopePositionForPlayer(p);
			DataPacket dp = new DataPacket(-1);
	        dp.setBalances(balances);
	        dp.setRopePos(ropepos);
	        playerToChannelMap.get(p).writeAndFlush(dp);
		}
	}
	
	private float calculateBalanceForPlayer(Player player) {
		float sum = 0;
		for(Player p: channelToPlayerMap.values()) {
	        sum += p.getBufferedKg();
		}
		float balance = player.getBufferedKg()/sum;
		player.appendBalanceToRopePos(balance);
		return balance;
	}
	
	private float calculateRopePositionForPlayer(Player player) {
		float sum = 0;
		for(Player p: channelToPlayerMap.values()) {
	        sum += p.getRopePos();
		}
		return (float) (((player.getRopePos() / sum) -0.5) * 2);
	}
	
	public void addPlayer(String name, Channel c){
		Player p = new Player(this.playerIDs++, name);
		this.playerToChannelMap.put(p, c);
		this.channelToPlayerMap.put(c, p);
		this.allClients.writeAndFlush(new ConnectPacket(p.getName(), p.getId()));
		printOnlinePlayers();
	}
	
	public void removePlayer(Channel c) {
		Player p = this.channelToPlayerMap.get(c);
		this.channelToPlayerMap.remove(c);
		this.playerToChannelMap.remove(p);
		printOnlinePlayers();
	}
	public void updatePlayerKg(Channel channel, float kg) {
		this.channelToPlayerMap.get(channel).addLatestKg(kg);
	}
	
	private void printOnlinePlayers() {
		System.out.print("[server] players online: ");
		for(Player p: channelToPlayerMap.values()) {
	        System.out.print("'" + p.getName() + "' ");
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws IOException {
		RemotugServer s = new RemotugServer("0.0.0.0", 4575);
		
		//RemoteClient c = new RemoteClient("127.0.0.1", 12345);
		
		System.out.println("[server] online");
		System.in.read();
		System.out.println("[server] shutdown");
		
		//c.shutdown();
		s.shutdown();
	}
	
}
