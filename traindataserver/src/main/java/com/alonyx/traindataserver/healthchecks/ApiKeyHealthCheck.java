package com.alonyx.traindataserver.healthchecks;

import com.codahale.metrics.health.HealthCheck;

public class ApiKeyHealthCheck extends HealthCheck {
    private final String apiKey;

    public ApiKeyHealthCheck(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected Result check() throws Exception {
        if (null == this.apiKey ||  apiKey.isEmpty()) {
            return Result.unhealthy("api-key is not valid");
        }
        return Result.healthy();
    }
}
