package fi.uef.remotug.net.server;

public class Player {
	private int id = -1;
	private String name = "null";
	private float bufferedKg = 0;
	private float ropePos = 0;

	private boolean readyForMatch = false;

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
		System.out.println("[server] new player > '" + name + "', " + id);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public float getBufferedKg() {
		return bufferedKg;
	}

	public void addLatestKg(float latestKg) {
		this.bufferedKg = (latestKg + this.bufferedKg * 2) / 3;
	}

	public float getRopePos() {
		return ropePos;
	}
	
	public void appendBalanceToRopePos(float balance) {
		this.ropePos += balance;
	}
	
	public void resetRopePos() {
		this.ropePos = 0;
	}
	
	public boolean isReadyForMatch() {
		return readyForMatch;
	}

	public void setReadyForMatch(boolean readyForMatch) {
		this.readyForMatch = readyForMatch;
	}
}
