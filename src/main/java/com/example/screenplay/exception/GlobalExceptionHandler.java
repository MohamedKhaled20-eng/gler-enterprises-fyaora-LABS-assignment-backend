package com.example.screenplay.exception;

import com.example.screenplay.config.ErrorMessagesConfig;
import com.example.screenplay.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * Uses centralized error messages from configuration.
 * Follows best practices from agentdesktop project.
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMessagesConfig errorMessagesConfig;

    /**
     * Handles IllegalArgumentException (text validation errors).
     * Returns 400 Bad Request with no body.
     *
     * @return empty bad request response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request due to illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    /**
     * Handles ExternalApiException (upstream service failures).
     * Returns 502 Bad Gateway with detailed error information.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 502 status
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApi(ExternalApiException ex, HttpServletRequest request) {
        log.error("External API error: {} [Code: {}]", ex.getMessage(), ex.getErrorCode(), ex);

        ErrorMessagesConfig.ErrorDetail errorDetail =
                errorMessagesConfig.getGeneric().get("upstream-error");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error(errorDetail.getMessage())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles validation errors (missing or invalid request fields).
     * Returns 400 Bad Request with validation error details.
     *
     * @param request the HTTP request
     * @return error response with 400 status
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> handleValidation(HttpServletRequest request) {
        log.warn("Validation error for request: {}", request.getRequestURI());

        ErrorMessagesConfig.ErrorDetail errorDetail =
                errorMessagesConfig.getValidation().get("missing-fields");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(errorDetail.getMessage())
                .errorCode(errorDetail.getCode())
                .message(errorDetail.getDescription())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all unexpected exceptions.
     * Returns 500 Internal Server Error with generic error details.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return error response with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: ", ex);

        ErrorMessagesConfig.ErrorDetail errorDetail =
                errorMessagesConfig.getGeneric().get("internal-error");

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(errorDetail.getMessage())
                .errorCode(errorDetail.getCode())
                .message(ex.getMessage() != null ? ex.getMessage() : errorDetail.getDescription())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
