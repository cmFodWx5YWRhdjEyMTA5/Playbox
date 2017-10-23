package uk.co.darkerwaters;

import java.util.ArrayList;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

public class CommsPortManager {
	private static CommsPortManager INSTANCE = new CommsPortManager();
	
	private SerialPort[] ports = new SerialPort[0];

	private SerialPort selectedPort;
	
	public interface CommsPortManagerListener {
		public void onCommsPortDataReceived(final String string);
	};
	
	private final ArrayList<CommsPortManagerListener> listeners;
	
	private CommsPortManager() {
		// constructor
		this.listeners = new ArrayList<CommsPortManagerListener>();
	}
	
	public static CommsPortManager getManager() {
		return INSTANCE;
	}
	
	public String[] listPorts()
    {
        ArrayList<String> portNames = new ArrayList<String>();
        this.ports = SerialPort.getCommPorts();
		for (SerialPort commsPort : this.ports) {
			portNames.add(commsPort.getDescriptivePortName());
        }        
        return portNames.toArray(new String[portNames.size()]);
    }

	public boolean setSelectedPort(String item) {
		// find the correct item
		this.selectedPort = null;
		for (SerialPort commsPort : this.ports) {
			String portName = commsPort.getDescriptivePortName(); 
			if (null != portName && portName.equals(item)) {
				// this is the one
				this.selectedPort = commsPort;
				break;
			}
        }
		return null != this.selectedPort;
	}

	public SerialPort getSelectedPort() {
		return this.selectedPort;
	}
	
	public static String ParityToString(int parity) {
		switch (parity) {
		case SerialPort.NO_PARITY :
			return "none";
		case SerialPort.ODD_PARITY :
			return "odd";
		case SerialPort.EVEN_PARITY :
			return "even";
		case SerialPort.MARK_PARITY :
			return "mark";
		case SerialPort.SPACE_PARITY :
			return "space";
		default :
			return "unknown";
		}
	}
	
	public static int StringToParity(String parity) {
		switch (parity) {
		case "none" :
			return SerialPort.NO_PARITY;
		case "odd" :
			return SerialPort.ODD_PARITY;
		case "even" :
			return SerialPort.EVEN_PARITY;
		case "mark" :
			return SerialPort.MARK_PARITY;
		case "space" :
			return SerialPort.SPACE_PARITY;
		default :
			return SerialPort.NO_PARITY;
			
		}
	}
	
	public static String StopBitsToString(int stopBits) {
		switch (stopBits) {
		case SerialPort.ONE_STOP_BIT :
			return "1";
		case SerialPort.ONE_POINT_FIVE_STOP_BITS :
			return "1.5";
		case SerialPort.TWO_STOP_BITS :
			return "2";
		default :
			return "unknown";
		}
	}
	
	public static int StringToStopBits(String stopBits) {
		switch (stopBits) {
		case "1" :
			return SerialPort.ONE_STOP_BIT;
		case "1.5" :
			return SerialPort.ONE_POINT_FIVE_STOP_BITS;
		case "2" :
			return SerialPort.TWO_STOP_BITS;
		default :
			return SerialPort.ONE_STOP_BIT;
			
		}
	}

	public boolean isConnected() {
		return null != this.selectedPort && this.selectedPort.isOpen();
	}

	public boolean disconnect() {
		if (null == this.selectedPort || !this.selectedPort.isOpen()) {
			return false;
		}
		return this.selectedPort.closePort();
	}

	public boolean connect() {
		if (null == this.selectedPort) {
			// fail
			return false;
		}
		if (!this.selectedPort.openPort()) {
			// fail
			return false;
		}
		// create the reader
		this.selectedPort.addDataListener(new SerialPortDataListener() {
			StringBuilder dataReceived = new StringBuilder();
			@Override
			public int getListeningEvents() {
				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_DATA_WRITTEN;
			}

			@Override
			public void serialEvent(SerialPortEvent event) {
				switch(event.getEventType()) {
					case SerialPort.LISTENING_EVENT_DATA_AVAILABLE :
						// handle reading the data
						SerialPort port = event.getSerialPort();
						// here when there is data available to read, read it
						int available = port.bytesAvailable();
						if (available > 0) {
							byte[] newData = new byte[port.bytesAvailable()];
							int numRead = port.readBytes(newData, newData.length);
							for (int i = 0; i < numRead; ++i) {
								// for each byte read, add to the collector
								char newChar = (char)newData[i];
								if (newChar == '\r' || newChar == '\n') {
									// this is the end of data, process the line received
									if (dataReceived.length() > 0) {
										CommsPortManager.this.onDataReceived(dataReceived.toString());
										// and clear the data
										dataReceived.delete(0, dataReceived.length());
									}
								}
								else {
									// append the data
									dataReceived.append(newChar);
								}
							}
						}
						break;
					case SerialPort.LISTENING_EVENT_DATA_WRITTEN :
						System.out.println("All bytes were successfully transmitted!");
						break;
				}
			}
		});
		// return the success
		return true;
	}
	
	public boolean addListener(CommsPortManagerListener listener) {
		boolean result;
		synchronized (this.listeners) {
			result = this.listeners.add(listener);
		}
		return result;
	}
	
	public boolean removeListener(CommsPortManagerListener listener) {
		boolean result;
		synchronized (this.listeners) {
			result = this.listeners.remove(listener);
		}
		return result;
	}

	protected void onDataReceived(final String string) {
		// inform any listeners of this change
		synchronized (this.listeners) {
			for (CommsPortManagerListener listener : this.listeners) {
				listener.onCommsPortDataReceived(string);
			}
		}
	}
}
