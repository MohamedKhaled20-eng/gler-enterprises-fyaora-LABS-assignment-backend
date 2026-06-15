package com.example.screenplay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for API endpoints and paths.
 * Loads API configuration from application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "api")
public class ApiConfig {

    private String basePath;
    private Endpoints endpoints;

    @Data
    public static class Endpoints {
        private String textReplace;
        private String forecast;
    }
}

