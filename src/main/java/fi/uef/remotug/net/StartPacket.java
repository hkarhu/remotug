package fi.uef.remotug.net;

public class StartPacket extends BasePacket{

	@Override
	public PacketType getType() {
		return PacketType.start;
	}

}
