package com.example.screenplay.repository;

import com.example.screenplay.model.ForecastRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ForecastRepositoryTest {

    @Autowired
    private ForecastRepository repository;

    @Test
    void shouldPersistAndLoadRecord() {
        ForecastRecord record = new ForecastRecord(LocalDate.parse("2026-05-04"), 22.7, 69.0, 15.4);
        repository.save(record);

        var loaded = repository.findById(LocalDate.parse("2026-05-04"));
        assertTrue(loaded.isPresent());
        assertEquals(22.7, loaded.get().getMaxTemperature());
        assertEquals(69.0, loaded.get().getMaxHumidity());
        assertEquals(15.4, loaded.get().getMaxWindSpeed());
    }
}
