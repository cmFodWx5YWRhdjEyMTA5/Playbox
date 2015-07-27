package com.alonyx.traindataserver;

import org.slf4j.LoggerFactory;

import com.alonyx.traindataserver.data.DataCollectionManager;
import com.alonyx.traindataserver.healthchecks.ConfigurationFileHealthCheck;
import com.alonyx.traindataserver.resources.GatherResource;
import com.alonyx.traindataserver.resources.StationResource;
import com.alonyx.traindataserver.resources.TrainResource;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TrainDataServerApp extends Application<TrainDataServerConfiguration> {
    
	public static void main(String[] args) throws Exception {
        new TrainDataServerApp().run(args);
    }

    @Override
    public String getName() {
        return "Alonyx Train Data";
    }

    @Override
    public void initialize(Bootstrap<TrainDataServerConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(TrainDataServerConfiguration configuration, Environment environment) {
    	// register health checks
    	environment.healthChecks().register("configuration-file", 
    			new ConfigurationFileHealthCheck(
    					configuration.getApiKey(),
    					configuration.getStoragePath(),
    					configuration.getStorageDelay()));
        // register resources
	    environment.jersey().register(new GatherResource());
	    environment.jersey().register(new StationResource());
	    environment.jersey().register(new TrainResource());
	    
	    // and startup the data collection manager
		try {
			// create the manager
			DataCollectionManager dataCollectionManager = new DataCollectionManager(configuration.getStorageDelay(), configuration.getStoragePath());
			// manage the created manager
		    environment.lifecycle().manage(dataCollectionManager);
		} catch (InstantiationException e) {
			// log this - will run but not store data?
			LoggerFactory.getLogger(getClass()).error("Failed to create the data collection manager: " + e.getMessage());
		}
    }

}
