package com.example.screenplay.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuration for REST clients used in the application.
 * Configures timeout and other HTTP client settings.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final ForecastApiConfig forecastApiConfig;

    /**
     * Creates a configured RestClient bean for external API calls.
     * Includes timeout settings from configuration.
     *
     * @param builder RestClient builder
     * @return configured RestClient instance
     */
    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        log.info("Initializing RestClient with base URL: {}", forecastApiConfig.getBaseUrl());

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(forecastApiConfig.getTimeout().getConnect());
        requestFactory.setReadTimeout(forecastApiConfig.getTimeout().getRead());

        return builder
                .baseUrl(forecastApiConfig.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}

