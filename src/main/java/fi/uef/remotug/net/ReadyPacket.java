package fi.uef.remotug.net;

public class ReadyPacket extends BasePacket {
	private int userId;
	private String userName;
	
	public ReadyPacket(int id, String name) {
		this.userId = id;
		this.userName = name;
	}
	
	public int getUserId() {
		return this.userId;
	}
	
	public String getUserName() {
		return this.userName;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.ready;
	}

}
