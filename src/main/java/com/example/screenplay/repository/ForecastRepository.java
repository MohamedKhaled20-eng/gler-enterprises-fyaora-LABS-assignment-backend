package com.example.screenplay.repository;

import com.example.screenplay.model.ForecastRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ForecastRepository extends JpaRepository<ForecastRecord, LocalDate> {
}
