package com.example.screenplay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for external forecast API.
 * Loads forecast API settings from application.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "forecast.api")
public class ForecastApiConfig {

    private String baseUrl;
    private Endpoints endpoints;
    private Params params;
    private Timeout timeout;

    @Data
    public static class Endpoints {
        private String currentForecast;
    }

    @Data
    public static class Params {
        private Double latitude;
        private Double longitude;
        private String hourly;
    }

    @Data
    public static class Timeout {
        private Integer connect;
        private Integer read;
    }

    /**
     * Build the complete forecast URI with query parameters
     *
     * @return formatted URI string
     */
    public String buildForecastUri() {
        return String.format("%s?latitude=%s&longitude=%s&hourly=%s",
                endpoints.getCurrentForecast(),
                params.getLatitude(),
                params.getLongitude(),
                params.getHourly());
    }
}


