package com.example.screenplay.controller;

import com.example.screenplay.config.ApiConfig;
import com.example.screenplay.service.TextReplaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for text replacement operations.
 * Handles character replacement in text strings.
 * URL mappings are externalized to application.yml
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TextReplaceController {

    private final TextReplaceService textReplaceService;
    private final ApiConfig apiConfig;

    /**
     * Replaces first and last characters of input text.
     * GET /replace?text={value}
     *
     * @param text input text to process (optional parameter)
     * @return processed text or empty response based on business rules
     */
    @GetMapping("/replace")
    public ResponseEntity<String> replaceText(@RequestParam(value = "text", required = false) String text) {
        log.info("Received text replacement request");

        String result = textReplaceService.replaceCharacters(text);

        if (result.isEmpty()) {
            log.debug("Returning empty response for 2-character input");
            return ResponseEntity.ok().build();
        }

        log.debug("Successfully processed text replacement");
        return ResponseEntity.ok(result);
    }
}
