package com.example.screenplay.service;

import com.example.screenplay.config.ErrorMessagesConfig;
import com.example.screenplay.config.ForecastApiConfig;
import com.example.screenplay.dto.ForecastRequest;
import com.example.screenplay.dto.OpenMeteoResponse;
import com.example.screenplay.exception.ExternalApiException;
import com.example.screenplay.model.ForecastRecord;
import com.example.screenplay.repository.ForecastRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Service for processing weather forecast data.
 * Implements best practices including:
 * - Lombok for reduced boilerplate
 * - Stream API for optimal performance (O(n) complexity)
 * - Externalized configuration
 * - Comprehensive error handling with custom messages
 * - SLF4J logging
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForecastService {

    private final ForecastRepository repository;
    private final RestClient restClient;
    private final ForecastApiConfig apiConfig;
    private final ErrorMessagesConfig errorMessagesConfig;

    /**
     * Processes forecast data from external API and saves to database.
     * Time Complexity: O(n) where n is the number of hourly data points
     * Space Complexity: O(1) as we only store aggregated max values
     *
     * @param request forecast request with date and field selection flags
     * @return saved forecast record
     * @throws ExternalApiException if API is unreachable or returns invalid data
     */
    public ForecastRecord processAndSaveForecast(ForecastRequest request) {
        log.debug("Processing forecast request for date: {}", request.date());

        OpenMeteoResponse response = fetchForecastData();
        validateResponse(response);

        List<String> times = response.hourly().time();
        List<Integer> targetIndices = findTargetIndices(times, request.date());

        log.debug("Found {} matching time entries for date {}", targetIndices.size(), request.date());

        // Extract max values only for requested fields
        // Using ternary operator for null-safe conditional assignment
        Double maxTemp = Boolean.TRUE.equals(request.addTemprature())
                ? findMaxValue(response.hourly().temperature_2m(), targetIndices)
                : null;
        Double maxHumid = Boolean.TRUE.equals(request.addHumidity())
                ? findMaxValue(response.hourly().relative_humidity_2m(), targetIndices)
                : null;
        Double maxWind = Boolean.TRUE.equals(request.addWindSpeed())
                ? findMaxValue(response.hourly().wind_speed_10m(), targetIndices)
                : null;

        ForecastRecord record = new ForecastRecord(
                LocalDate.parse(request.date()),
                maxTemp,
                maxHumid,
                maxWind
        );

        ForecastRecord savedRecord = repository.save(record);
        log.info("Successfully saved forecast record for date: {}", savedRecord.getDate());

        return savedRecord;
    }

    /**
     * Fetches forecast data from external weather API.
     *
     * @return OpenMeteoResponse containing hourly weather data
     * @throws ExternalApiException if connection fails
     */
    private OpenMeteoResponse fetchForecastData() {
        try {
            log.debug("Fetching forecast data from external API: {}", apiConfig.getBaseUrl());

            OpenMeteoResponse response = restClient.get()
                    .uri(apiConfig.buildForecastUri())
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            log.debug("Successfully fetched forecast data from external API");
            return response;
        } catch (Exception ex) {
            log.error("Failed to connect to external forecast API", ex);
            ErrorMessagesConfig.ErrorDetail errorDetail =
                    errorMessagesConfig.getForecast().get("api-unreachable");
            throw new ExternalApiException(
                    errorDetail.getMessage(),
                    errorDetail.getCode(),
                    ex
            );
        }
    }

    /**
     * Validates that the API response contains required data.
     *
     * @param response the API response to validate
     * @throws ExternalApiException if response is null or incomplete
     */
    private void validateResponse(OpenMeteoResponse response) {
        if (response == null || response.hourly() == null || response.hourly().time() == null) {
            log.error("Received empty or invalid response from external API");
            ErrorMessagesConfig.ErrorDetail errorDetail =
                    errorMessagesConfig.getForecast().get("empty-data");
            throw new ExternalApiException(
                    errorDetail.getMessage(),
                    errorDetail.getCode()
            );
        }
    }

    /**
     * Finds indices of time entries matching the target date.
     * Uses Stream API for efficient filtering.
     * Time Complexity: O(n) where n is the size of times list
     *
     * @param times list of timestamp strings
     * @param targetDate date string to match (YYYY-MM-DD format)
     * @return list of matching indices
     */
    private List<Integer> findTargetIndices(List<String> times, String targetDate) {
        return IntStream.range(0, times.size())
                .filter(i -> times.get(i) != null && times.get(i).startsWith(targetDate))
                .boxed()
                .toList();
    }

    /**
     * Finds the maximum value from a list at specified indices.
     * Uses Stream API for functional and efficient processing.
     * Time Complexity: O(m) where m is the size of indices list
     *
     * @param values list of values to search
     * @param indices list of indices to consider
     * @return maximum value or null if no valid values found
     */
    private Double findMaxValue(List<Double> values, List<Integer> indices) {
        if (values == null || indices == null || indices.isEmpty()) {
            return null;
        }

        return indices.stream()
                .filter(i -> i < values.size() && values.get(i) != null)
                .map(values::get)
                .max(Double::compareTo)
                .orElse(null);
    }
}
