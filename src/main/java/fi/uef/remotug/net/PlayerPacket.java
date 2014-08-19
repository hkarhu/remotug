package fi.uef.remotug.net;

public class PlayerPacket extends BasePacket {

	private final String content;

	public PlayerPacket(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
	
}
