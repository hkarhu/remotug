package ropepull;


public class RopePull {

	private static Sensor sensor = new Sensor();
	private static ServerConnection serverConnection = new ServerConnection();
	private static RopeGUI gui = new RopeGUI();
	
	public static void main(String[] args) {
		
		//ServerConnection listens for changes on the rope
		sensor.addListener(serverConnection);
		sensor.start();
		
		//ServerConnection gives out information about the game status to the gui
		serverConnection.addListener(gui);
		serverConnection.connect();
		
		gui.startGL();
		
	}
	
}
