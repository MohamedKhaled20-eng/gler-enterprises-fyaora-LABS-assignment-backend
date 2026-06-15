package com.example.screenplay.integration;

import com.example.screenplay.repository.ForecastRepository;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ForecastApiIntegrationTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("forecast.api.base-url", wireMock::baseUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ForecastRepository repository;

    @Test
    void shouldProcessForecastAndPersistRecord() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(okJson("""
                        {
                          "hourly": {
                            "time": ["2026-05-04T00:00", "2026-05-04T01:00", "2026-05-05T00:00"],
                            "temperature_2m": [20.2, 22.7, 18.5],
                            "relative_humidity_2m": [50, 69, 80],
                            "wind_speed_10m": [10.1, 15.4, 5.2]
                          }
                        }
                        """)));

        String request = """
                {
                  "date": "2026-05-04",
                  "addTemprature": true,
                  "addHumidity": false,
                  "addWindSpeed": true
                }
                """;

        mockMvc.perform(post("/api/v1/forcast")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxTemperature").value(22.7))
                .andExpect(jsonPath("$.maxHumidity").value(nullValue()))
                .andExpect(jsonPath("$.maxWindSpeed").value(15.4));

        assert repository.findById(java.time.LocalDate.parse("2026-05-04")).isPresent();
    }

    @Test
    void shouldReturn502WhenWireMockReturnsError() throws Exception {
        wireMock.stubFor(get(urlPathEqualTo("/v1/forecast"))
                .willReturn(aResponse().withStatus(500)));

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
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Upstream API Unreachable"));
    }
}
