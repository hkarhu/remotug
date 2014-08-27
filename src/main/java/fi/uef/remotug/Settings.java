package fi.uef.remotug;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Settings implements Serializable {

	public static final String SETTINGS_FILE = "settings.dat";
	
	private String playerName;
	private int playerID;
	private String serverAddress;
	private int serverPort;
	private String sensorPort;
	private int sensorSpeed;
	
	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public int getPlayerID() {
		return this.playerID;
	}

	public void setPlayerID(int id) {
		this.playerID = id;
	}
	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public String getSensorPort() {
		return sensorPort;
	}

	public void setSensorPort(String sensorPort) {
		this.sensorPort = sensorPort;
	}

	public int getSensorSpeed() {
		return sensorSpeed;
	}

	public void setSensorSpeed(int sensorSpeed) {
		this.sensorSpeed = sensorSpeed;
	}

	public static void saveSettings(Settings s){
		try {
			FileOutputStream fileOut = new FileOutputStream(SETTINGS_FILE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(s);
			out.close();
			fileOut.close();
			System.out.printf("Saved settings to " + SETTINGS_FILE);
		} catch(IOException i) {
			i.printStackTrace();
		}
	}

	public static Settings loadSettings(){
		Settings s = null;
		try {
			FileInputStream fileIn = new FileInputStream(SETTINGS_FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			s = (Settings) in.readObject();
			in.close();
			fileIn.close();
		} catch(ClassNotFoundException c) {
			c.printStackTrace();
			return null;
		} catch(FileNotFoundException e) {
			return null;
		} catch(IOException i) {
			i.printStackTrace();
			return null;
		} 
		return s;
	}

	public void print() {
		System.out.println("Sensor speed " + sensorSpeed);
	}	
}
