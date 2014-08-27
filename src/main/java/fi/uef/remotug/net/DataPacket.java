package fi.uef.remotug.net;

import java.util.HashMap;

public class DataPacket extends BasePacket {
	private float kg = 0;
	private HashMap<Integer, Float> balances;
	private float ropepos = 0;

	public DataPacket(float kg) {
		this.kg = kg;
	}
	
	public float getKg() {
		return kg;
	}

	public void setKg(float kg) {
		this.kg = kg;
	}
	
	public HashMap<Integer, Float> getBalances() {
		return this.balances;
	}

	public void setBalances(HashMap<Integer, Float> balances) {
		this.balances = balances;
	}
	
	public float getRopePos() {
		return ropepos;
	}

	public void setRopePos(float ropepos) {
		this.ropepos = ropepos;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.data;
	}

}
