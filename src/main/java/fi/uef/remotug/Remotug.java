package fi.uef.remotug;

import java.io.IOException;

import fi.conf.ae.routines.S;
import fi.uef.remotug.net.client.Connection;
import fi.uef.remotug.sensor.Sensor;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;


public class Remotug {
	
	public static void main(String[] args) {
		
		Sensor sensor;
		Connection connection;
		RopeGUI gui = new RopeGUI();
		Settings settings = Settings.loadSettings();
		
		if(settings == null) settings = new Settings();
		
		SetupDialog s = new SetupDialog(settings);
		
		if(!s.userSelectedConnect()){
			return;
		}
		
		S.debug("Creating connection to server...");
		connection = new Connection(settings.getServerAddress(), settings.getServerPort());
		
		S.debug("Creating connection to sensor...");
		sensor = new Sensor(settings.getSensorPort(), settings.getSensorSpeed());
		
		//ServerConnection listens for changes on the rope
		sensor.addListener(connection);  
		sensor.addListener(gui);
		
		try {
			sensor.start("/dev/tty.usbserial-A501S2BY");
			//sensor.start("");
		} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
			e.printStackTrace();
		}
		
		//ServerConnection gives out information about the game status to the gui
		connection.addListener(gui);
		
		gui.startGL();
		
	}
	
}
