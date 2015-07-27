package com.alonyx.traindataserver.healthchecks;

import com.codahale.metrics.health.HealthCheck;

public class ConfigurationFileHealthCheck extends HealthCheck {
    private final String apiKey;
    private final String storagePath;
    private final String storageDelay;
    private final String gatherInterval;

    public ConfigurationFileHealthCheck(String apiKey, String storagePath, String storageDelay, String gatherInterval) {
        this.apiKey = apiKey;
        this.storagePath = storagePath;
        this.storageDelay = storageDelay;
        this.gatherInterval = gatherInterval;
    }

    @Override
    protected Result check() throws Exception {
        if (null == this.apiKey ||  apiKey.isEmpty()) {
            return Result.unhealthy("api-key is not valid");
        }
        if (null == this.storagePath ||  
        	storagePath.isEmpty() ||
        	storagePath.charAt(storagePath.length() - 1) != '/') {
            return Result.unhealthy("storage-path is not valid, specifiy a path ending in '/'");
        }
        if (null == this.storageDelay ||  storageDelay.isEmpty()) {
            return Result.unhealthy("storage-delay is not valid");
        }
        else {
        	try {
        		long storeDelay = Long.parseLong(this.storageDelay);
        		if (storeDelay < 1000l || storeDelay > 50000000000l) {
                    return Result.unhealthy("storage-delay is not valid, specifiy a number of ms more than 1000 and less than 50000000000");
                }
        	}
        	catch (NumberFormatException e) {
        		return Result.unhealthy("storage-delay is not valid: " + e.getMessage());
        	}
        }
        if (null == this.gatherInterval ||  gatherInterval.isEmpty()) {
            return Result.unhealthy("gather-interval is not valid");
        }
        else {
        	try {
        		long gatherTime = Long.parseLong(this.gatherInterval);
        		if (gatherTime < 1000l || gatherTime > 50000000000l) {
                    return Result.unhealthy("gather-interval is not valid, specifiy a number of ms more than 1000 and less than 50000000000");
                }
        	}
        	catch (NumberFormatException e) {
        		return Result.unhealthy("gather-interval is not valid: " + e.getMessage());
        	}
        }
        // if here all is good
        return Result.healthy();
    }
}
