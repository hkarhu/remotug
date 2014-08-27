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
	
	private ChannelGroup allClients;
	private RemotugServer server; 
	public ServersideHandler(ChannelGroup allClients, RemotugServer server) {
		this.allClients = allClients;
		this.server = server;
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
		server.removePlayer(ctx.channel());
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
			server.addPlayer(cp.getPlayerName(), ctx.channel());
			break;
		case ready: 
			System.out.println("[server] received a ready-packet");
			if(server.matchStarted() != RemotugServer.NO_ACTIVE_MATCH){
				System.out.println("[server] ready packet ignored because of ongoing match");
				break;
			}
			ReadyPacket rp = (ReadyPacket)p;
			server.playerReady(ctx.channel());
			break;
		case data: 
			//System.out.println("[server] received a data-packet");
			DataPacket dp = (DataPacket)p;
			server.updatePlayerKg(ctx.channel(), dp.getKg());
			//channelToPlayerMap.get(ctx.channel()).addLatestKg(dp.getKg());
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
	
	
}
