package fi.uef.remotug.sensor;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SerialPortHandler {

	private static boolean continueToRead = true;
	
	public void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
		if ( portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);

			if ( commPort instanceof SerialPort ) {
				SerialPort serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				InputStream in = serialPort.getInputStream();
				(new Thread(new SerialReader(in), "reader")).start();
			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}     
	}

	public static class SerialReader implements Runnable {
		
		InputStream in;
		
		public SerialReader ( InputStream in ) {
			this.in = in;
		}
		
		@Override
		public void run (){
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (continueToRead){
				try {
					
					//output data etc...
					
				} catch(NumberFormatException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	public void close() { 
		continueToRead = false; 
	}

}
