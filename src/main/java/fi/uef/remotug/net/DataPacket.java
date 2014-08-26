package fi.uef.remotug.net;

public class DataPacket extends BasePacket {
	private float kg = 0;
	private float balance = 0;

	public DataPacket(float kg) {
		this.kg = kg;
	}
	
	public float getKg() {
		return kg;
	}

	public void setKg(float kg) {
		this.kg = kg;
	}
	
	public float getBalance() {
		return balance;
	}

	public void setBalance(float balance) {
		this.balance = balance;
	}
	
	@Override
	public PacketType getType() {
		return PacketType.data;
	}

}
