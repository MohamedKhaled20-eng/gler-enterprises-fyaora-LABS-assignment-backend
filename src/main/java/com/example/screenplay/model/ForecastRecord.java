package com.example.screenplay.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Entity representing a forecast record in the database.
 * Uses Lombok annotations to reduce boilerplate code.
 */
@Entity
@Table(name = "forecast_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForecastRecord {

    @Id
    private LocalDate date;

    private Double maxTemperature;
    private Double maxHumidity;
    private Double maxWindSpeed;
}
