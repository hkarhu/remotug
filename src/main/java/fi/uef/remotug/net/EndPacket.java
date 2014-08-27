package fi.uef.remotug.net;

public class EndPacket extends BasePacket {
	private int winnerId;
	
	public EndPacket(int id) {
		this.winnerId = id;
	}
	
	public int getWinnerId() {
		return this.winnerId;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.end;
	}

}
