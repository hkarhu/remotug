package fi.uef.remotug.net.client;

import java.util.List;

import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.StartPacket;
import fi.uef.remotug.net.server.Player;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ClientsideHandler extends ChannelHandlerAdapter {
	
	public ClientsideHandler(List<ConnectionListener> serverListeners) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("Beep!");
		
//		switch (p.getType()) {
//		case connect:
//			ConnectPacket cp = (ConnectPacket)p;
//			Player player = new Player(playerIDs++, cp.getPlayerName());
//			addPlayer(player, ctx.channel());
//			allClients.writeAndFlush(cp);
//			break;
//		case start: 
//			StartPacket sp = (StartPacket)p;
//			break;
//		case data: 
//			DataPacket dp = (DataPacket)p;
//			channelToPlayerMap.get(ctx.channel()).addLatestKg(dp.getKg());
//			calculateAndSendBalances(dp);
//			break;
//		//case stop: break;
//
//		default:
//			System.err.println("Received valid base packet but unknown class type! Client newer than server?");
//			break;
//		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
