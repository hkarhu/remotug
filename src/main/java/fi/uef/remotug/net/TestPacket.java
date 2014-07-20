package fi.uef.remotug.net;

public class TestPacket extends BasePacket {

	private final String content;

	public TestPacket(String content) {
		this.content = content;
	}

	public String getContent() {
		return content;
	}
	
}
