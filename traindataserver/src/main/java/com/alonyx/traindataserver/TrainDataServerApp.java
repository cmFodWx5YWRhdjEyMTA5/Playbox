package com.alonyx.traindataserver;

import com.alonyx.traindataserver.healthchecks.ApiKeyHealthCheck;
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
    	environment.healthChecks().register("api-key", new ApiKeyHealthCheck(configuration.getDefaultApiKey()));
        // register resources
	    environment.jersey().register(new GatherResource());
	    environment.jersey().register(new StationResource());
	    environment.jersey().register(new TrainResource());
    }

}
