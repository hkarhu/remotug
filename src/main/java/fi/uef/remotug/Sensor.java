package ropepull;

import java.util.ArrayList;
import java.util.List;

public class Sensor {

	private List<SensorListener> sensorListeners = new ArrayList<>();
	
	public Sensor() {
		// TODO Everything
	}
	
	public void start(){
		// TODO Start generating sensor change events.
	}
	
	public void addListener(SensorListener	sensorListener) {
		this.sensorListeners.add(sensorListener);
	}
	
	private void announceSensorChange(float kg){
		for(SensorListener l : sensorListeners){
			l.newSensorDataArrived(kg);
		}
	}

}
