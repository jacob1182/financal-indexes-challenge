package com.example.financialindexes.api;

import com.example.financialindexes.app.TickApplicationService;
import com.example.financialindexes.domain.Statistics;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.financialindexes.TickUtils.genTick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TickControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TickApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService.clearTicks();
    }


    @Test
    void receiveTick() throws Exception {
        var tick = genTick(59);

        mvc.perform(post("/ticks")
                    .content(convertToJson(tick))
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        assertEquals(tick.getPrice(), applicationService.getStatistics().getMax());
        assertEquals(tick.getPrice(), applicationService.getStatistics().getMin());
        assertEquals(tick.getPrice(), applicationService.getStatistics().getSum());
        assertEquals(1, applicationService.getStatistics().getCount());
    }

    @Test
    void receiveInvalidTick() throws Exception {
        var tick = genTick(61);

        mvc.perform(post("/ticks")
                .content(convertToJson(tick))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertEquals(Statistics.EMPTY, applicationService.getStatistics());
    }

    private <T> String convertToJson(T value) throws JsonProcessingException {
        return new JsonMapper().writeValueAsString(value);
    }
}