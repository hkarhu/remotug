package fi.uef.remotug.net;

public class StartPacket extends BasePacket{
	private long startTime;
	private int matchDuration;
	private int matchStartDelay;
	
	public StartPacket(long startTime, int matchDuration, int matchStartDelay) {
		this.startTime = startTime;
		this.matchDuration = matchDuration;
		this.matchStartDelay = matchStartDelay;
	}
	
	public long getStartTime() {
		return this.startTime;
	}
	
	public int getMatchDuration() {
		return this.matchDuration;
	}
	
	public long getMatchStartDelay() {
		return this.matchStartDelay;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.start;
	}

}
