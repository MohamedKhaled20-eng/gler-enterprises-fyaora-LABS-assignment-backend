package com.example.screenplay.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * Standard error response structure with error codes and descriptions.
 * Uses Lombok @Builder for flexible object creation.
 */
@Builder
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String path
) {
}
