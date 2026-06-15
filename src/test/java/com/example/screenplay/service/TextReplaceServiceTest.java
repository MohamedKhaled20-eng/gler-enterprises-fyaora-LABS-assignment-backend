package com.example.screenplay.service;

import com.example.screenplay.config.ErrorMessagesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TextReplaceServiceTest {

    @Mock
    private ErrorMessagesConfig errorMessagesConfig;

    private TextReplaceService service;

    @BeforeEach
    void setUp() {
        ErrorMessagesConfig.ErrorDetail nullInputError = new ErrorMessagesConfig.ErrorDetail();
        nullInputError.setCode("TR001");
        nullInputError.setMessage("Text cannot be null");

        ErrorMessagesConfig.ErrorDetail tooShortError = new ErrorMessagesConfig.ErrorDetail();
        tooShortError.setCode("TR002");
        tooShortError.setMessage("Text is too short");

        lenient().when(errorMessagesConfig.getTextReplace()).thenReturn(Map.of(
                "null-input", nullInputError,
                "too-short", tooShortError
        ));

        service = new TextReplaceService(errorMessagesConfig);
    }

    @Test
    void shouldReturnBadRequestForNull() {
        assertThrows(IllegalArgumentException.class, () -> service.replaceCharacters(null));
    }

    @Test
    void shouldReturnBadRequestForLengthLessThan2() {
        assertThrows(IllegalArgumentException.class, () -> service.replaceCharacters(""));
        assertThrows(IllegalArgumentException.class, () -> service.replaceCharacters("a"));
    }

    @Test
    void shouldReturnEmptyForLength2() {
        assertEquals("", service.replaceCharacters("ab"));
    }

    @Test
    void shouldReplaceFirstAndLastForLengthGreaterThan2() {
        assertEquals("*b$", service.replaceCharacters("abc"));
        assertEquals("*om$", service.replaceCharacters("home"));
        assertEquals("*lephan$", service.replaceCharacters("elephant"));
    }

    @Test
    void shouldHandleMixedText() {
        assertEquals("*bc#20xy$", service.replaceCharacters("abc#20xyz"));
        assertEquals("*y Test Projec$", service.replaceCharacters("My Test Project"));
        assertEquals("*estingCodeAssignmentProjec$", service.replaceCharacters("TestingCodeAssignmentProject"));
    }
}
