package fi.uef.remotug.net.client;

import java.util.HashMap;
import java.util.List;

import fi.uef.remotug.Remotug;
import fi.uef.remotug.net.BasePacket;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.DataPacket;
import fi.uef.remotug.net.EndPacket;
import fi.uef.remotug.net.ReadyPacket;
import fi.uef.remotug.net.StartPacket;
import fi.uef.remotug.net.server.Player;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ClientsideHandler extends ChannelHandlerAdapter {
	
	private List<ConnectionListener> serverListeners;
	public ClientsideHandler(List<ConnectionListener> serverListeners) {
		this.serverListeners = serverListeners;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		//System.out.println("[client] server data");
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
			if(cp.getPlayerName().compareTo(Remotug.settings.getPlayerName()) == 0) {
				Remotug.settings.setPlayerID(cp.getPlayerID());
				System.out.println("[client] my id seems to be '" + Remotug.settings.getPlayerID() + "', awesome! :3");
			} else {
				System.out.println("[client] user named '" + cp.getPlayerName() + "' connected to game session");
			}
			break;
		case ready:
			ReadyPacket rp = (ReadyPacket)p;
			System.out.println("[client] user with id '" + rp.getUserId() + "' is ready to play!");
			readyAnnounced(rp.getUserId());
			break;
		case start: 
			StartPacket sp = (StartPacket)p;
			System.out.println("[client] match started");
			startAnnounced(sp.getStartTime(), sp.getMatchDuration(), sp.getMatchStartDelay());
			break;
		case data: 
			DataPacket dp = (DataPacket)p;
			gameValuesChanged(dp.getRopePos(), dp.getBalances());
			break;
		case end:
			EndPacket ep = (EndPacket)p;
			System.out.println("[client] match ended, winner id is '" + ep.getWinnerId() + "'");
			winnerAnnounced(ep.getWinnerId());
			break;
		default:
			System.err.println("Received valid base packet but unknown class type! Server newer than client?");
			break;
		}
	}

	private void gameValuesChanged(float ropePos, HashMap<Integer, Float> balances) {
		for(ConnectionListener l: this.serverListeners) {
			l.gameValuesChanged(ropePos, balances);
		}
	}
	
	private void readyAnnounced(int playerID) {
		for(ConnectionListener l: this.serverListeners) {
			l.readyAnnounced(playerID);
		}
	}
	
	private void startAnnounced(long serverTime, int duration, int delay) {
		for(ConnectionListener l: this.serverListeners) {
			l.startAnnounced(serverTime, duration, delay);
		}
	}
	
	private void winnerAnnounced(int playerID) {
		for(ConnectionListener l: this.serverListeners) {
			l.winnerAnnounced(playerID);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
	}
}
