package com.example.screenplay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class ScreenplayAssignmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreenplayAssignmentApplication.class, args);
    }
}
