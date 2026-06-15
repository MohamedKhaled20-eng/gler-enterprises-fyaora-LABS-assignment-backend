package com.example.screenplay.exception;

import com.example.screenplay.config.ErrorMessagesConfig;
import com.example.screenplay.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ErrorMessagesConfig errorMessagesConfig;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        ErrorMessagesConfig.ErrorDetail upstreamError = new ErrorMessagesConfig.ErrorDetail();
        upstreamError.setCode("SYS002");
        upstreamError.setMessage("Upstream API Unreachable");

        ErrorMessagesConfig.ErrorDetail validationError = new ErrorMessagesConfig.ErrorDetail();
        validationError.setCode("VAL001");
        validationError.setMessage("Missing or invalid mandatory fields");
        validationError.setDescription("One or more required fields are missing or contain invalid values");

        ErrorMessagesConfig.ErrorDetail internalError = new ErrorMessagesConfig.ErrorDetail();
        internalError.setCode("SYS001");
        internalError.setMessage("Internal Server Error");
        internalError.setDescription("An unexpected error occurred while processing the request");

        lenient().when(errorMessagesConfig.getGeneric()).thenReturn(Map.of(
                "upstream-error", upstreamError,
                "internal-error", internalError
        ));
        lenient().when(errorMessagesConfig.getValidation()).thenReturn(Map.of(
                "missing-fields", validationError
        ));

        handler = new GlobalExceptionHandler(errorMessagesConfig);
    }

    @Test
    void shouldReturnBadRequestForIllegalArgument() {
        ResponseEntity<Void> response = handler.handleIllegalArgument(new IllegalArgumentException("test"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(null, response.getBody());
    }

    @Test
    void shouldReturnBadGatewayErrorPayload() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/forcast");

        ResponseEntity<ErrorResponse> response = handler.handleExternalApi(
                new ExternalApiException("Connection to the upstream is unreachable", "FC001"),
                request
        );

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(502, response.getBody().status());
        assertEquals("Upstream API Unreachable", response.getBody().error());
        assertEquals("FC001", response.getBody().errorCode());
        assertEquals("/api/v1/forcast", response.getBody().path());
    }

    @Test
    void shouldReturnBadRequestValidationPayload() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/forcast");

        ResponseEntity<ErrorResponse> response = handler.handleValidation(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("VAL001", response.getBody().errorCode());
    }

    @Test
    void shouldReturnInternalServerErrorPayload() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/forcast");

        ResponseEntity<ErrorResponse> response = handler.handleAllUnexpected(
                new RuntimeException("unexpected"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("SYS001", response.getBody().errorCode());
    }
}
