package ropepull;

import java.util.ArrayList;
import java.util.List;

public class ServerConnection implements SensorListener {

	private List<ServerConnectionListener> serverListeners = new ArrayList<>();
	
	public ServerConnection() {
		// TODO Everything necessary
	}
	
	public void addListener(ServerConnectionListener listener) {
		this.serverListeners.add(listener);
	}
	
	//Balance could be ie. a value from -1.0f to 1.0f
	private void announceBalanceChange(float balance){
		for(ServerConnectionListener l : serverListeners){
			l.gameBalanceChanged(balance);
		}
	}
	
	public void connect(){
		//TODO all the connecting stuff
	}
	
	@Override
	public void newSensorDataArrived(float kg) {
		// TODO Auto-generated method stub
		
	}

}
