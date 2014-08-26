package fi.uef.remotug.net.server;

public class Player {
	private int id = 0;
	private String name = "null";
	private float bufferedKg = 0;

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
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
		this.bufferedKg = (latestKg + this.bufferedKg * 5) / 6;
	}
}
