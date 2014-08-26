package fi.uef.remotug.net.server;

import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.StartPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ServersideHandler extends ChannelHandlerAdapter {
	
	private ChannelGroup allClients;

	private final ConcurrentHashMap<Player, Channel> playerToChannelMap = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Channel, Player> channelToPlayerMap = new ConcurrentHashMap<>();
	
	private int playerIDs = 0;
	
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
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("[server] client data");
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
			System.out.println("[server] received connect-packet");
			ConnectPacket cp = (ConnectPacket)p;
			Player player = new Player(playerIDs++, cp.getPlayerName());
			addPlayer(player, ctx.channel());
			allClients.writeAndFlush(cp);
			break;
		case start: 
			System.out.println("[server] received start-packet");
			StartPacket sp = (StartPacket)p;
			break;
		case data: 
			System.out.println("[server] received data-packet");
			DataPacket dp = (DataPacket)p;
			channelToPlayerMap.get(ctx.channel()).addLatestKg(dp.getKg());
			calculateAndSendBalances(dp);
			break;
		//case stop: break;

		default:
			System.err.println("Received valid base packet but unknown class type! Client newer than server?");
			break;
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
	
	private void channelInActive(Channel channel) {
		this.allClients.remove(channel);
	}
	
	private void calculateAndSendBalances(DataPacket dp) {
		for(Player p: channelToPlayerMap.values()) {
			float balance = calculateBalanceForPlayer(p);
	        dp.setBalance(balance);
	        playerToChannelMap.get(p).writeAndFlush(dp);
		}
	}
	
	private float calculateBalanceForPlayer(Player player) {
		float sum = 0;
		for(Player p: channelToPlayerMap.values()) {
	        sum += p.getBufferedKg();
		}
		return player.getBufferedKg()/sum;
	}
	
	private void addPlayer(Player p, Channel c){
		playerToChannelMap.put(p, c);
		channelToPlayerMap.put(c, p);
	}
}
