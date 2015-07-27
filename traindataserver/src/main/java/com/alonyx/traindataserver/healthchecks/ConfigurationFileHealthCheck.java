package com.alonyx.traindataserver.healthchecks;

import com.codahale.metrics.health.HealthCheck;

public class ConfigurationFileHealthCheck extends HealthCheck {
    private final String apiKey;
    private final String storagePath;
    private final long storageDelay;

    public ConfigurationFileHealthCheck(String apiKey, String storagePath, long storageDelay) {
        this.apiKey = apiKey;
        this.storagePath = storagePath;
        this.storageDelay = storageDelay;
    }

    @Override
    protected Result check() throws Exception {
        if (null == this.apiKey ||  apiKey.isEmpty()) {
            return Result.unhealthy("api-key is not valid");
        }
        if (null == this.storagePath ||  
        	storagePath.isEmpty() ||
        	storagePath.charAt(storagePath.length() - 1) != '/') {
            return Result.unhealthy("storage path is not valid, specifiy a path ending in '/'");
        }
        if (storageDelay < 1000l || storageDelay > 50000000000l) {
            return Result.unhealthy("storage delay is not valid, specifiy a number of ms more than 1000 and less than 50000000000");
        }
        return Result.healthy();
    }
}
