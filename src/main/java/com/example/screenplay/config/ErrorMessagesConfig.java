package com.example.screenplay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Configuration class for error messages.
 * Loads error codes and descriptions from error-messages.yml
 */
@Data
@Component
@ConfigurationProperties(prefix = "errors")
public class ErrorMessagesConfig {

    private Map<String, ErrorDetail> textReplace;
    private Map<String, ErrorDetail> forecast;
    private Map<String, ErrorDetail> validation;
    private Map<String, ErrorDetail> generic;

    @Data
    public static class ErrorDetail {
        private String code;
        private String message;
        private String description;
    }
}

