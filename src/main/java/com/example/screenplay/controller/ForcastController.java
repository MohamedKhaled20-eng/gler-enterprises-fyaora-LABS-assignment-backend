package com.example.screenplay.controller;

import com.example.screenplay.config.ApiConfig;
import com.example.screenplay.dto.ForecastRequest;
import com.example.screenplay.model.ForecastRecord;
import com.example.screenplay.service.ForecastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for forecast operations.
 * Handles weather forecast data retrieval and storage.
 * URL mappings are externalized to application.yml
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ForcastController {

    private final ForecastService forecastService;
    private final ApiConfig apiConfig;

    /**
     * Retrieves and stores weather forecast data for a specific date.
     * POST /api/v1/forcast
     * Also supports legacy endpoint: /forcast
     *
     * @param request validated forecast request with date and field flags
     * @return saved forecast record with HTTP 200 OK
     */
    @PostMapping({"/forcast", "/api/v1/forcast"})
    public ResponseEntity<ForecastRecord> getForecast(@Valid @RequestBody ForecastRequest request) {
        log.info("Received forecast request for date: {}", request.date());

        ForecastRecord savedRecord = forecastService.processAndSaveForecast(request);

        log.info("Successfully processed forecast request for date: {}", savedRecord.getDate());
        return ResponseEntity.ok(savedRecord);
    }
}
