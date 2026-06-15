package com.example.screenplay.service;

import com.example.screenplay.config.ErrorMessagesConfig;
import com.example.screenplay.config.ForecastApiConfig;
import com.example.screenplay.dto.ForecastRequest;
import com.example.screenplay.exception.ExternalApiException;
import com.example.screenplay.model.ForecastRecord;
import com.example.screenplay.repository.ForecastRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private ForecastRepository repository;

    @Mock
    private ErrorMessagesConfig errorMessagesConfig;

    private MockRestServiceServer mockServer;
    private ForecastService service;
    private ForecastApiConfig apiConfig;

    @BeforeEach
    void setUp() {
        // Setup API config
        apiConfig = new ForecastApiConfig();
        apiConfig.setBaseUrl("https://api.open-meteo.com");

        ForecastApiConfig.Endpoints endpoints = new ForecastApiConfig.Endpoints();
        endpoints.setCurrentForecast("/v1/forecast");
        apiConfig.setEndpoints(endpoints);

        ForecastApiConfig.Params params = new ForecastApiConfig.Params();
        params.setLatitude(52.52);
        params.setLongitude(13.41);
        params.setHourly("temperature_2m,relative_humidity_2m,wind_speed_10m");
        apiConfig.setParams(params);

        // Setup error messages config
        ErrorMessagesConfig.ErrorDetail errorDetail = new ErrorMessagesConfig.ErrorDetail();
        errorDetail.setCode("FC001");
        errorDetail.setMessage("Connection to the upstream is unreachable");

        ErrorMessagesConfig.ErrorDetail emptyDataError = new ErrorMessagesConfig.ErrorDetail();
        emptyDataError.setCode("FC002");
        emptyDataError.setMessage("Upstream API returned empty data");

        lenient().when(errorMessagesConfig.getForecast()).thenReturn(Map.of(
                "api-unreachable", errorDetail,
                "empty-data", emptyDataError
        ));

        RestClient.Builder builder = RestClient.builder().baseUrl(apiConfig.getBaseUrl());
        mockServer = MockRestServiceServer.bindTo(builder).build();

        service = new ForecastService(repository, builder.build(), apiConfig, errorMessagesConfig);
    }

    @Test
    void shouldFetchComputeAndSaveMaxValues() {
        String body = """
                {
                  "hourly": {
                    "time": ["2026-05-04T00:00", "2026-05-04T01:00", "2026-05-05T00:00"],
                    "temperature_2m": [20.2, 22.7, 18.5],
                    "relative_humidity_2m": [50, 69, 80],
                    "wind_speed_10m": [10.1, 15.4, 5.2]
                  }
                }
                """;

        mockServer.expect(requestTo(apiConfig.getBaseUrl() + apiConfig.buildForecastUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        when(repository.save(any(ForecastRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForecastRequest request = new ForecastRequest("2026-05-04", true, true, true);
        ForecastRecord saved = service.processAndSaveForecast(request);

        assertNotNull(saved);
        assertEquals(LocalDate.parse("2026-05-04"), saved.getDate());
        assertEquals(22.7, saved.getMaxTemperature());
        assertEquals(69.0, saved.getMaxHumidity());
        assertEquals(15.4, saved.getMaxWindSpeed());

        ArgumentCaptor<ForecastRecord> captor = ArgumentCaptor.forClass(ForecastRecord.class);
        verify(repository).save(captor.capture());
        assertEquals(LocalDate.parse("2026-05-04"), captor.getValue().getDate());
        mockServer.verify();
    }

    @Test
    void shouldStoreNullForDisabledFields() {
        String body = """
                {
                  "hourly": {
                    "time": ["2026-05-04T00:00", "2026-05-04T01:00"],
                    "temperature_2m": [20.2, 22.7],
                    "relative_humidity_2m": [50, 69],
                    "wind_speed_10m": [10.1, 15.4]
                  }
                }
                """;

        mockServer.expect(requestTo(apiConfig.getBaseUrl() + apiConfig.buildForecastUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        when(repository.save(any(ForecastRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForecastRecord saved = service.processAndSaveForecast(new ForecastRequest("2026-05-04", true, false, false));
        assertEquals(22.7, saved.getMaxTemperature());
        assertNull(saved.getMaxHumidity());
        assertNull(saved.getMaxWindSpeed());

        mockServer.verify();
    }

    @Test
    void shouldThrow502WhenUpstreamUnavailable() {
        mockServer.expect(requestTo(apiConfig.getBaseUrl() + apiConfig.buildForecastUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThrows(ExternalApiException.class,
                () -> service.processAndSaveForecast(new ForecastRequest("2026-05-04", true, true, true)));

        mockServer.verify();
    }

    @Test
    void shouldThrowWhenUpstreamReturnsEmptyBody() {
        mockServer.expect(requestTo(apiConfig.getBaseUrl() + apiConfig.buildForecastUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ExternalApiException exception = assertThrows(ExternalApiException.class,
                () -> service.processAndSaveForecast(new ForecastRequest("2026-05-04", true, true, true)));

        assertTrue(exception.getMessage().contains("empty data"));
        mockServer.verify();
    }

    @Test
    void shouldPersistNullMaximumsWhenNoHoursForDate() {
        String body = """
                {
                  "hourly": {
                    "time": ["2026-05-05T00:00"],
                    "temperature_2m": [18.5],
                    "relative_humidity_2m": [80],
                    "wind_speed_10m": [5.2]
                  }
                }
                """;

        mockServer.expect(requestTo(apiConfig.getBaseUrl() + apiConfig.buildForecastUri()))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        when(repository.save(any(ForecastRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ForecastRecord saved = service.processAndSaveForecast(new ForecastRequest("2026-05-04", true, true, true));
        assertNull(saved.getMaxTemperature());
        assertNull(saved.getMaxHumidity());
        assertNull(saved.getMaxWindSpeed());

        mockServer.verify();
    }
}
