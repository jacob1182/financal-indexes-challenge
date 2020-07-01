package com.example.financialindexes.api;

import com.example.financialindexes.domain.Tick;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

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
        var body = genTickJson();
        mvc.perform(post("/ticks")
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    private String genTickJson() throws JsonProcessingException {
        var tick = new Tick("IBM.N", BigDecimal.valueOf(143.82), 1478192204000L);
        return new JsonMapper().writeValueAsString(tick);
    }
}