package com.alonyx.traindataserver;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TrainDataServerConfiguration extends Configuration {

    @NotEmpty
    private String apiKey = "c3917141-7b8c-4c5c-b628-0d1a06939eb7";
    
    @NotEmpty
    private String storagePath = "./stations/";
    
    @NotEmpty
    private String storageDelay = "300000";
    
    @NotEmpty
    private String gatherInterval = "60000";

    @JsonProperty("api-key")
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty("api-key")
    public void setApiKey(String key) {
        this.apiKey = key;
    }
    
    @JsonProperty("storage-path")
    public String getStoragePath() {
    	return this.storagePath;
    }
    
    @JsonProperty("storage-path")
    public void setStoragePath(String path) {
    	this.storagePath = path;
    }
    
    @JsonProperty("storage-delay")
    public String getStorageDelay() {
    	return this.storageDelay;
    }
    
    @JsonProperty("storage-delay")
    public void setStorageDelay(String delay) {
    	this.storageDelay = delay;
    }
    
    @JsonProperty("gather-interval")
    public String getGatherInterval() {
    	return this.gatherInterval;
    }
    
    @JsonProperty("gather-interval")
    public void setGatherInterval(String interval) {
    	this.gatherInterval = interval;
    }
}
