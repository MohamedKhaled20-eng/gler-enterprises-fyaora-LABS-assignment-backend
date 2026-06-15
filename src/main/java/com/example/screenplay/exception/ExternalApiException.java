package com.example.screenplay.exception;

import lombok.Getter;

/**
 * Exception thrown when external API calls fail.
 * Includes error code for better error tracking and handling.
 */
@Getter
public class ExternalApiException extends RuntimeException {

    private final String errorCode;

    public ExternalApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ExternalApiException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
