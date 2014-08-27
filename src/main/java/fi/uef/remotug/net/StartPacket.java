package fi.uef.remotug.net;

public class StartPacket extends BasePacket{
	private long startTime;
	
	public StartPacket(long stime) {
		this.startTime = stime;
	}
	@Override
	public PacketType getType() {
		return PacketType.start;
	}

}
