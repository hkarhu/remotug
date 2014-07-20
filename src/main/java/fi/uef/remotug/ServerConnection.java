package fi.uef.remotug;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import fi.conf.ae.routines.S;
import fi.uef.remotug.sensor.SensorListener;

public class ServerConnection implements SensorListener {

	private List<ServerConnectionListener> serverListeners = new ArrayList<>();

	private Timer simulationTimer;

	public ServerConnection() {
		//Random function generator for testing...
		simulationTimer = new Timer(33, new ActionListener() {
			int current = 0, target = 4007;
			float q = 1, out = 0;
			@Override
			public void actionPerformed(ActionEvent e) {
				current += q*9999;
				if((q > 0 && current >= target) || (q < 0 && current <= target)){
					target = (int) (-q*((target*target+1)%(101*4007)));
					q = -q;
				}
				out = ((float)((1+(current/(float)(101*4007)))/2.0f)*0.05f + out*0.95f);
				announceBalanceChange(1-2*out);
			}
		});

		simulationTimer.setRepeats(true);
		simulationTimer.start();
	}

	public void addListener(ServerConnectionListener listener) {
		this.serverListeners.add(listener);
	}

	//Balance could be ie. a value from -1.0f to 1.0f
	private void announceBalanceChange(float balance){
		//S.debug("Balance: " + balance);
		for(ServerConnectionListener l : serverListeners){
			l.gameBalanceChanged(balance);
		}
	}

	//Win or lose!
	private void announceWinner(int winnerID){
		for(ServerConnectionListener l : serverListeners){
			l.winnerAnnounced(winnerID);
		}
	}
	
	public void connect(){
		//TODO all the connecting stuff
	}

	public void close(){
		simulationTimer.stop();
	}

	@Override
	public void newSensorDataArrived(float kg) {
		// TODO Auto-generated method stub

	}

}
