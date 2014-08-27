package fi.uef.remotug.net.server;

import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.EndPacket;
import fi.uef.remotug.net.ReadyPacket;
import fi.uef.remotug.net.StartPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class ServersideHandler extends ChannelHandlerAdapter {
	
	public static final int NO_ACTIVE_MATCH = -1;
	public static final int MATCH_LENGTH = 30000;
	
	private ChannelGroup allClients;

	private final ConcurrentHashMap<Player, Channel> playerToChannelMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Channel, Player> channelToPlayerMap = new ConcurrentHashMap<>();
	
	private int playerIDs = 0;
	private long matchStarted = NO_ACTIVE_MATCH;
	
	public ServersideHandler(ChannelGroup allClients) {
		this.allClients = allClients;
	}
	
//	-- echo server protocol:
//	@Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		allClients.add(ctx.channel());
//		
//		// -- welcome!
//		
//		ByteBuf buf = ctx.alloc().buffer();
//		buf.writeBytes(new String("Welcome!\n").getBytes(StandardCharsets.UTF_8));
//		ctx.channel().writeAndFlush(buf);
//		
//		/* -- bytebuf reference counting
//		 * correct? or sent items are released by netty? src:
//		 * ahh, ok: "When an outbound (a.k.a. downstream) message reaches at the beginning of the pipeline,
//		 * 			 Netty will release it after writing it out."
//		 * http://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-11
//		 * http://netty.io/wiki/reference-counted-objects.html
//		 */
//		//buf.release();
//	}
//	
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//		ctx.writeAndFlush(msg);
//	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[server] client connected");
		this.allClients.add(ctx.channel());
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[server] client disconnected");
		this.allClients.remove(ctx.channel());
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//System.out.println("[server] client data");
		BasePacket p;
		
		try {
			p = (BasePacket) msg;
		} catch (ClassCastException e) {
			System.err.println("Received invalid packet! Disconnected client.");
			ctx.channel().close();
			return;
		}
		
		switch (p.getType()) {
		case connect:
			System.out.println("[server] received a connect-packet");
			ConnectPacket cp = (ConnectPacket)p;
			Player player = new Player(playerIDs++, cp.getPlayerName());
			addPlayer(player, ctx.channel());
			allClients.writeAndFlush(cp);
			break;
		case ready: 
			System.out.println("[server] received a ready-packet");
			if(this.matchStarted != NO_ACTIVE_MATCH){
				System.out.println("[server] ready packet ignored because of ongoing match");
				break;
			}
			ReadyPacket rp = (ReadyPacket)p;
			playerReady(this.channelToPlayerMap.get(ctx.channel()));
			break;
		case data: 
			//System.out.println("[server] received a data-packet");
			DataPacket dp = (DataPacket)p;
			channelToPlayerMap.get(ctx.channel()).addLatestKg(dp.getKg());
			break;
		default:
			System.err.println("Received valid base packet but unknown class type! Client newer than server?");
			break;
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
	
	private void playerReady(Player player) {
		player.setReadyForMatch(true);
		
		boolean allReady = true;
		for(Player p: this.channelToPlayerMap.values()) {
			if(!p.isReadyForMatch()) allReady = false;
		}
		
		this.allClients.writeAndFlush(new ReadyPacket(player.getId()));
		
		if(allReady) {
			System.out.println("[server] starting a new match in 5 seconds");
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				  public void run() {
					  startMatch();
				  }
				}, 5000);
		}
	}
	
	private void startMatch() {
		System.out.println("[server] match started");
		for(Player p: this.channelToPlayerMap.values()) {
			p.resetRopePos();
		}
		this.matchStarted = System.currentTimeMillis();
		this.allClients.writeAndFlush(new StartPacket(this.matchStarted));
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
			if(p.getRopePos() > bestRopePos) winner = p;
		}
		System.out.println("[server] match winner > " + winner.getName() + ", " + winner.getId());
		
		this.allClients.writeAndFlush(new EndPacket(winner.getId()));
	}
	
	public void calculateAndSendBalances() {
		DataPacket dp = new DataPacket(-1);
		HashMap<Integer, Float> balances = new HashMap<Integer, Float>();
		for(Player p: channelToPlayerMap.values()) {
			balances.put(p.getId(), calculateBalanceForPlayer(p));
		}
		for(Player p: channelToPlayerMap.values()) {
			float ropepos = calculateRopePositionForPlayer(p);
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
		player.appendBalanceToRoperPos(balance);
		return balance;
	}
	
	private float calculateRopePositionForPlayer(Player player) {
		float sum = 0;
		for(Player p: channelToPlayerMap.values()) {
	        sum += p.getRopePos();
		}
		return (float) (((player.getRopePos() / sum) -0.5) * 2);
	}
	
	private void addPlayer(Player p, Channel c){
		playerToChannelMap.put(p, c);
		channelToPlayerMap.put(c, p);
	}
}
