package fi.uef.remotug;

import java.io.IOException;

import fi.uef.remotug.sensor.Sensor;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;


public class Remotug {

	private static Sensor sensor = new Sensor();
	private static ServerConnection serverConnection = new ServerConnection();
	private static RopeGUI gui = new RopeGUI();
	
	public static void main(String[] args) {
		
		//ServerConnection listens for changes on the rope
		sensor.addListener(serverConnection);  
		try {
			//sensor.start("/dev/ttyUSB0");
			sensor.start("");
		} catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException | IOException e) {
			e.printStackTrace();
		}
		
		//ServerConnection gives out information about the game status to the gui
		serverConnection.addListener(gui);
		serverConnection.connect();
		
		gui.startGL();
		
	}
	
}
