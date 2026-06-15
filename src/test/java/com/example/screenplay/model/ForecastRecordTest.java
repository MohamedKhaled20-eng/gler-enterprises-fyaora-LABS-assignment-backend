package com.example.screenplay.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ForecastRecordTest {

    @Test
    void shouldCreateRecordUsingAllArgsConstructor() {
        ForecastRecord record = new ForecastRecord(LocalDate.parse("2026-05-04"), 22.7, 69.0, 15.4);

        assertEquals(LocalDate.parse("2026-05-04"), record.getDate());
        assertEquals(22.7, record.getMaxTemperature());
        assertEquals(69.0, record.getMaxHumidity());
        assertEquals(15.4, record.getMaxWindSpeed());
    }

    @Test
    void shouldSetAndGetAllFields() {
        ForecastRecord record = new ForecastRecord();

        assertNull(record.getDate());
        assertNull(record.getMaxTemperature());
        assertNull(record.getMaxHumidity());
        assertNull(record.getMaxWindSpeed());

        record.setDate(LocalDate.parse("2026-05-04"));
        record.setMaxTemperature(22.7);
        record.setMaxHumidity(69.0);
        record.setMaxWindSpeed(15.4);

        assertEquals(LocalDate.parse("2026-05-04"), record.getDate());
        assertEquals(22.7, record.getMaxTemperature());
        assertEquals(69.0, record.getMaxHumidity());
        assertEquals(15.4, record.getMaxWindSpeed());
    }
}
