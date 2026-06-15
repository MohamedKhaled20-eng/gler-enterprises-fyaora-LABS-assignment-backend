package com.example.screenplay.dto;

import java.util.List;

public record OpenMeteoResponse(HourlyData hourly) {

    public record HourlyData(
            List<String> time,
            List<Double> temperature_2m,
            List<Double> relative_humidity_2m,
            List<Double> wind_speed_10m
    ) {
    }
}
