package com.example.financialindexes.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TickControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void receiveTick() throws Exception {
        String body =
                "{\n" +
                "\"instrument\": \"IBM.N\",\n" +
                "\"price\": 143.82,\n" +
                "\"timestamp\": 1478192204000\n" +
                "}";
        mvc.perform(post("/ticks").content(body))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }
}