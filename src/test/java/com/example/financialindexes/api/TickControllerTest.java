package com.example.financialindexes.api;

import com.example.financialindexes.TickUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

        var body = genTickJson(59);
        mvc.perform(post("/ticks")
                    .content(body)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }

    @Test
    void receiveInvalidTick() throws Exception {
        var body = genTickJson(60);
        mvc.perform(post("/ticks")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    private String genTickJson(long secondsOlder) throws JsonProcessingException {
        return new JsonMapper().writeValueAsString(TickUtils.genTick(secondsOlder));
    }
}