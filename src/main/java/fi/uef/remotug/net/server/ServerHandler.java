package fi.uef.remotug.net.server;

import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.StartPacket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ServerHandler extends ChannelHandlerAdapter {
	
	private ChannelGroup allClients;

	private final HashMap<Player, Channel> playerToChannelMap = new HashMap<>();
	private final HashMap<Channel, Player> channelToPlayerMap = new HashMap<>();
	
	private int playerIDs = 0;
	
	public ServerHandler(ChannelGroup allClients) {
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
			ConnectPacket cp = (ConnectPacket)p;
			Player player = new Player(playerIDs++, cp.getPlayerName());
			addPlayer(player, ctx.channel());
			allClients.writeAndFlush(cp);
			break;
		case start: 
			StartPacket sp = (StartPacket)p;
			break;
		case data: 
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
	
	private void calculateAndSendBalances(DataPacket dp) {
		Iterator<Entry<Player, Channel>> it = playerToChannelMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<Player, Channel> pairs = (Map.Entry<Player, Channel>)it.next();
	        Player player = (Player)pairs.getKey();
	        Channel channel = (Channel)pairs.getValue();
	        float balance = calculateBalanceForPlayer(player);
	        dp.setBalance(balance);
	        channel.writeAndFlush(dp);
	    }
	}
	
	private float calculateBalanceForPlayer(Player player) {
		float sum = player.getBufferedKg();
		Iterator<Entry<Player, Channel>> it = playerToChannelMap.entrySet().iterator();
		while (it.hasNext()) {
	        Map.Entry<Player, Channel> pairs = (Map.Entry<Player, Channel>)it.next();
	        Player p = (Player)pairs.getKey();
	        if(!player.equals(p)) {
	        	sum += p.getBufferedKg();
	        }
	        pairs.getValue();
	    }
		return player.getBufferedKg()/sum;
	}
	
	private void addPlayer(Player p, Channel c){
		playerToChannelMap.put(p, c);
		channelToPlayerMap.put(c, p);
	}
}
