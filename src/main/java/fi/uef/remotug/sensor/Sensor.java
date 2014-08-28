package fi.uef.remotug.sensor;

import fi.conf.ae.routines.S;
import fi.uef.remotug.Remotug;
import fi.uef.remotug.Settings;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Sensor {

	private final List<SensorListener> sensorListeners = new ArrayList<>();
	private static boolean continueToRead;
	private Thread readerThread; 
	private String portName = "/dev/ttyUSB0";
	private int baudRate = 38400;

	public Sensor(String portName, int baudRate) {
		this.portName = portName;
		this.baudRate = baudRate;
	}

	public void start() throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {

		continueToRead = true;

		if(portName == "emulation"){
			S.debug("Initializing data emulator");

			readerThread = new Thread(new Runnable() {

				@Override
				public void run() {
					int current = 0, target = 4007;
					float q = 1, out = 0;
					while (continueToRead){
						try {
							current += q*9999;
							if((q > 0 && current >= target) || (q < 0 && current <= target)){
								target = (int) (-q*((target*target+1)%(101*4007)));
								q = -q;
							}
							out = ((float)((1+(current/(float)(101*4007)))/2.0f)*0.05f + out*0.95f);
							announceSensorChange(1500*out);
						} catch(NumberFormatException e2) {
							e2.printStackTrace();
						}

						try {
							Thread.sleep(33);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			});

			readerThread.start();

			return;
		}

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		if ( portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

			if ( commPort instanceof SerialPort ) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				final InputStream in = serialPort.getInputStream();
				S.debug("Connected serial device %s @ %s", serialPort.getName(), serialPort.getBaudBase());
				readerThread = new Thread(new Runnable() {
					@Override
					public void run() {
						BufferedReader reader = new BufferedReader(new InputStreamReader(in));
						while (continueToRead){
							try {
								String dataIdf = "W: ";
								String sData = reader.readLine();
								//S.debug("raw> " + sData);
								sData = sData.trim();
								if(sData.contains(dataIdf)) {
									String parsedData = sData.substring(sData.indexOf(dataIdf) + dataIdf.length());
									//S.debug("> " + parsedData);
									announceSensorChange(Float.parseFloat(parsedData));
								}
							} catch (IOException e) {
								e.printStackTrace();
							} catch(NumberFormatException e2) {
								e2.printStackTrace();
							}
						}
					}
				});

				readerThread.start();
			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}     
	}

	public void stop(){
		continueToRead = false;
	}

	public void addListener(SensorListener	sensorListener) {
		this.sensorListeners.add(sensorListener);
	}
	
	private void announceSensorChange(float kg){
		//Ebin!
		if(Settings.ebinStart){
			kg = (float)(kg*(0.035f*kg));
			if(kg > 9000) kg = 9001;
		}
		
		for(SensorListener l : sensorListeners){
			l.newSensorDataArrived(kg);
		}
	}

}
