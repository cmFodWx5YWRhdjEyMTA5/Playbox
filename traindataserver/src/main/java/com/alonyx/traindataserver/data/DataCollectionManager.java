package com.alonyx.traindataserver.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alonyx.traindataserver.resources.GatherResource;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.lifecycle.Managed;

public class DataCollectionManager implements Managed {
	
	private static final String K_FILEEXTENSION = ".stn";

	public static DataCollectionManager INSTANCE = null;
	
	private final ArrayList<StationData> stations;
	
	private final Thread storingThread;
	
	private final Thread gatheringThread;
	
	private final Object threadWait = new Object();
	
	private volatile boolean isProcessing = true;
	
	private volatile boolean isDirty = false;
	
	private final String storagePath;
	
	private final Logger LOG = LoggerFactory.getLogger(DataCollectionManager.class);
	
	private final ObjectMapper objectMapper;
	
    public DataCollectionManager(final long gatherInterval, final long storageDelay, String storagePath) throws InstantiationException {
    	if (null != INSTANCE) {
    		throw new InstantiationException("Don't create more than one data collection manager");
    	}
    	// remember the single instance
    	INSTANCE = this;
    	// and setup this class
    	this.storagePath = storagePath;
		this.objectMapper = new ObjectMapper();
		this.stations = new ArrayList<StationData>();
		// get the data from the store here
		loadStations();
		// now start the thread that will periodically store the data gathered
		this.storingThread = new Thread(new Runnable() {
			public void run() {
				// store and wait periodically
				LOG.info("Storage thread starting with " + Long.toString(storageDelay) + "ms delay between storage calls");
				while (isProcessing) {
					// store the stations
					storeStations();
					synchronized (threadWait) {
						try {
							// sleep the specified delay
							threadWait.wait(storageDelay);
						} catch (InterruptedException e) {
							// fine to interrupt
						}
					}
				}
				LOG.info("Storage thread ending");
			}
		}, "StationStoring");
		// and start the thread gathering data
		this.gatheringThread = new Thread(new Runnable() {
			public void run() {
				// gather data and wait periodically
				while (isProcessing) {
					// gather data
					new GatherResource().getTrains();
					synchronized (threadWait) {
						try {
							// sleep the specified delay
							threadWait.wait(storageDelay);
						} catch (InterruptedException e) {
							// fine to interrupt
						}
					}
				}
			}
		}, "DataGathering");
    }

    public void start() throws Exception {
    	// start up the threads
		this.storingThread.start();
		this.gatheringThread.start();
    }

    public void stop() throws Exception {
    	// stop the threads from running
        this.isProcessing = false;
        this.threadWait.notify();
    }
	
	public StationData[] getStations() {
		StationData[] stationData;
		synchronized (this.stations) {
			stationData = this.stations.toArray(new StationData[this.stations.size()]);
		}
		return stationData;
	}

	public void addStation(StationData stationData) {
		synchronized (this.stations) {
			this.stations.add(stationData);
			// change the dirty flag to store the change in data
			this.isDirty = true;
		}
	}

	public StationData getStation(String stationId) {
		StationData toReturn = null;
		synchronized (this.stations) {
			for (StationData stationData : this.stations) {
				if (stationData.getStationId().equals(stationId)) {
					// this is the station data, return this
					toReturn = stationData;
					break;
				}
			}
		}
		return toReturn;
	}
	
	private void loadStations() {
		synchronized (this.stations) {
			checkFolderExists();
			File theDir = new File(this.storagePath);
			// load all the stations in the directory
			for (File file : theDir.listFiles()) {
				if (file.exists() && file.isFile()) {
					// this is a file
					String filename = file.getName();
					String extension = "";
					int dotIndex = filename.lastIndexOf(".");
					if (dotIndex != -1) {
						extension = filename.substring(dotIndex, filename.length());
					}
					if (extension.equals(K_FILEEXTENSION)) {
						// this is a valid file, load this data
						loadFile(file, filename.substring(0, dotIndex));
					}
				}
			}
		}
	}
	
	private void loadFile(File file, String stationName) {
		// load the station data from this file 
		try { 
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader); 
			String stringData;
			StringBuilder fileContents = new StringBuilder();
			while((stringData = bufferedReader.readLine()) != null) { 
				fileContents.append(stringData);
			} 
			fileReader.close();
			// create the station data from this
			StationData stationData = createJacksonObject(StationData.class, fileContents.toString());
			addStation(stationData);
			if (false == stationData.getStationId().equals(stationName)) {
				// the ID isn't right
				LOG.error("Station data in \"" + file.getPath() + "\" is for " + stationData.getStationId() + " instead of " + stationName);
			}
        } catch (IOException e) {
            // log this error
        	LOG.error("Failed to load the data for " + stationName + " in " + file.getPath(), e);
        }
	}
	
	private void saveFile(StationData stationData) {
		File file = new File(this.storagePath + stationData.getStationId() + K_FILEEXTENSION);
		try { 
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(toJsonString(stationData));
            fileWriter.close();
        } catch (IOException e) {
            // log this error
        	LOG.error("Failed to store the data for " + stationData.getStationId() + " in " + file.getPath(), e);
        } 
	}

	private void checkFolderExists() {
		File theDir = new File(this.storagePath);
		// if the directory does not exist, create it 
		if (!theDir.exists()) {
			LOG.info("creating directory: " + this.storagePath);
		    boolean result = false;
		 
		    try{ 
		        theDir.mkdir();
		        result = true;
		    }  
		    catch(SecurityException se){
		        //handle it 
		    	LOG.error("Failed to create the directory \"" + this.storagePath + "\"", se);
		    }         
		    if(result) {    
		    	LOG.info("created directory: " + this.storagePath);
		    }
		}
	}

	private void storeStations() {
		// store data in the loop to protect file access from concurrent accessing
		synchronized (this.stations) {
			if (false == this.isDirty) {
				// no point
			}
			else {
				// ensure the folder is there
				checkFolderExists();
				for (StationData stationData : this.stations) {
					// for each station data item, store the file
					saveFile(stationData);
				}
				// the data is not dirty now we just stored it all
				this.isDirty = false;
			}
		}
	}
	
	public String toJsonString(Object object) {
		String jsonString = "error";
		try {
			jsonString = this.objectMapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (JsonMappingException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return jsonString;
	}
	
	public <T> T createJacksonObject(Class<T> jacksonClass, String jsonString) {
		// create the specified object from the database value
		T result = null;
		try {
			// read the value using jackson
			result = this.objectMapper.readValue(jsonString, jacksonClass);
		} catch (IOException e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
		return result;
	}

}
