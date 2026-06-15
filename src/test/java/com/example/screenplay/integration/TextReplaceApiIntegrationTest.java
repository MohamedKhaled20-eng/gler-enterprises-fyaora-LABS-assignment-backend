package com.example.screenplay.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TextReplaceApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnExpectedReplacement() throws Exception {
        mockMvc.perform(get("/replace").param("text", "abc#20xyz"))
                .andExpect(status().isOk())
                .andExpect(content().string("*bc#20xy$"));
    }

    @Test
    void shouldReturnBadRequestForSingleCharacter() throws Exception {
        mockMvc.perform(get("/replace").param("text", "x"))
                .andExpect(status().isBadRequest());
    }
}
