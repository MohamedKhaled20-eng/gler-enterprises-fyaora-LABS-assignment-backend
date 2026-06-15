package com.example.screenplay.service;

import com.example.screenplay.config.ErrorMessagesConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for text replacement operations.
 * Implements character replacement logic with validation.
 * Time Complexity: O(1) for all operations (constant time substring operations)
 * Space Complexity: O(n) where n is the length of input string
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextReplaceService {

    private final ErrorMessagesConfig errorMessagesConfig;

    /**
     * Replaces first and last characters of text with * and $ respectively.
     * Business rules:
     * - null input: throws IllegalArgumentException
     * - length < 2: throws IllegalArgumentException
     * - length = 2: returns empty string
     * - length > 2: replaces first char with * and last char with $
     *
     * @param text input string to process
     * @return processed string with replaced characters
     * @throws IllegalArgumentException if text is null or too short
     */
    public String replaceCharacters(String text) {
        log.debug("Processing text replacement for input: {}", text);

        if (text == null) {
            log.warn("Null text provided for replacement");
            ErrorMessagesConfig.ErrorDetail errorDetail =
                    errorMessagesConfig.getTextReplace().get("null-input");
            throw new IllegalArgumentException(errorDetail.getMessage());
        }

        if (text.length() < 2) {
            log.warn("Text too short for replacement: length = {}", text.length());
            ErrorMessagesConfig.ErrorDetail errorDetail =
                    errorMessagesConfig.getTextReplace().get("too-short");
            throw new IllegalArgumentException(errorDetail.getMessage());
        }

        if (text.length() == 2) {
            log.debug("Text length is 2, returning empty string");
            return "";
        }

        String result = "*" + text.substring(1, text.length() - 1) + "$";
        log.debug("Successfully replaced characters: {} -> {}", text, result);
        return result;
    }
}
