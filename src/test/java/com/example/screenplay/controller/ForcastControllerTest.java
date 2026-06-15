package com.example.screenplay.controller;

import com.example.screenplay.dto.ForecastRequest;
import com.example.screenplay.exception.ExternalApiException;
import com.example.screenplay.model.ForecastRecord;
import com.example.screenplay.service.ForecastService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForcastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForecastService forecastService;

    @Test
    void shouldReturn200AndBodyWhenValidRequest() throws Exception {
        when(forecastService.processAndSaveForecast(any(ForecastRequest.class)))
                .thenReturn(new ForecastRecord(LocalDate.parse("2026-05-04"), 22.7, 69.0, 15.4));

        String request = """
                {
                  "date": "2026-05-04",
                  "addTemprature": true,
                  "addHumidity": true,
                  "addWindSpeed": true
                }
                """;

        mockMvc.perform(post("/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-05-04"))
                .andExpect(jsonPath("$.maxTemperature").value(22.7))
                .andExpect(jsonPath("$.maxHumidity").value(69.0))
                .andExpect(jsonPath("$.maxWindSpeed").value(15.4));
    }

    @Test
    void shouldReturn400WhenMissingMandatoryField() throws Exception {
        String request = """
                {
                  "date": "2026-05-04",
                  "addTemprature": true,
                  "addWindSpeed": true
                }
                """;

        mockMvc.perform(post("/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn502WhenServiceThrowsExternalApiException() throws Exception {
        when(forecastService.processAndSaveForecast(any(ForecastRequest.class)))
                .thenThrow(new ExternalApiException("Connection to the upstream is unreachable", "FC001"));

        String request = objectMapper.writeValueAsString(new ForecastRequest("2026-05-04", true, true, true));

        mockMvc.perform(post("/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errorCode").value("FC001"));
    }
}
