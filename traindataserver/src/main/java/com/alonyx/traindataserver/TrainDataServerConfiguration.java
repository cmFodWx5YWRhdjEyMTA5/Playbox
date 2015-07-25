package com.alonyx.traindataserver;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TrainDataServerConfiguration extends Configuration {

    @NotEmpty
    private String apiKey = "c3917141-7b8c-4c5c-b628-0d1a06939eb7";

    @JsonProperty
    public String getApiKey() {
        return apiKey;
    }

    @JsonProperty
    public void setApiKey(String key) {
        this.apiKey = key;
    }
}
