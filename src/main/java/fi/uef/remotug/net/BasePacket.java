package fi.uef.remotug.net;

import java.io.Serializable;

public abstract class BasePacket implements Serializable {
	public abstract PacketType getType();
}
