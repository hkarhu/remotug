package fi.uef.remotug.net;

public class ConnectPacket extends BasePacket {
	private int id;
	private String name;

	public ConnectPacket(String playerName) {
		this.id = -1;
		this.name = playerName;
	}
	
	public void setPlayerID(int id){
		this.id = id;
	}

	public int getPlayerID() {
		return id;
	}
	
	public String getPlayerName() {
		return name;
	}

	public void setPlayerName(String name) {
		this.name = name;
	}

	@Override
	public PacketType getType() {
		return PacketType.connect;
	}
	
}
