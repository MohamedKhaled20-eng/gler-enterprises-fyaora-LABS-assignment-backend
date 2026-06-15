package com.example.screenplay.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ForecastRequest(
        @NotNull
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
        String date,
        @NotNull
        Boolean addTemprature,
        @NotNull
        Boolean addHumidity,
        @NotNull
        Boolean addWindSpeed
) {
}
