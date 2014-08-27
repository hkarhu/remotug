package fi.uef.remotug.net;

public class ReadyPacket extends BasePacket {
	private int userId;
	
	public ReadyPacket(int id) {
		this.userId = id;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.ready;
	}

}
