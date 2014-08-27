package fi.uef.remotug;

import java.io.IOException;

import javax.swing.JOptionPane;

import fi.conf.ae.routines.S;
import fi.uef.remotug.net.ConnectPacket;
import fi.uef.remotug.net.client.Connection;
import fi.uef.remotug.sensor.Sensor;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;


public class Remotug {
	
	private static Sensor sensor;
	private static Connection connection;
	private static RopeGUI gui;
	public static Settings settings = Settings.loadSettings();
	
	public static void main(String[] args) {
				
		if(settings == null) settings = new Settings();
		
		SetupDialog s = new SetupDialog(settings);
		
		if(!s.userSelectedConnect()){
			return;
		}
		
		S.debug("Creating connection to server...");
		
		connection = new Connection(settings.getServerAddress(), settings.getServerPort());
		if(!connection.isConnected()) {
			shutdown();
			return;
		} else {
			connection.writePacket(new ConnectPacket(settings.getPlayerName()));	
		}
		
		gui = new RopeGUI(connection);

		S.debug("Creating connection to sensor...");
		sensor = new Sensor(settings.getSensorPort(), settings.getSensorSpeed());
		
		//ServerConnection listens for changes on the rope
		sensor.addListener(connection);  
		sensor.addListener(gui);
		
		try {
			sensor.start();
		} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
			e.printStackTrace();
		}
		
		//ServerConnection gives out information about the game status to the gui
		connection.addListener(gui);
		
		gui.startGL();
		
		shutdown();
		
	}
	
	public static void shutdown(){
		if(connection != null) connection.close();
		if(gui != null) gui.requestClose();
		if(sensor != null) sensor.stop();
	}
	
	
}
