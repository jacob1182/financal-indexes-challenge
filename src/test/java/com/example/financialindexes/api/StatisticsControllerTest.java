package com.example.financialindexes.api;

import com.example.financialindexes.app.IndexApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.financialindexes.IndexUtils.genTick;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IndexApplicationService applicationService;

    @Test
    public void retrieveStatistics() throws Exception {

        var timestamp = System.currentTimeMillis();
        List.of(
                genTick(325, timestamp - 61_000),
                genTick(100, timestamp - 50_000),
                genTick(150, timestamp - 40_000),
                genTick(250, timestamp - 30_000),
                genTick(200, timestamp - 20_000)
        ).forEach(applicationService::receiveTick);

        mvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min", is(100d)))
                .andExpect(jsonPath("$.max", is(250d)))
                .andExpect(jsonPath("$.avg", is(700d/4)))
                .andExpect(jsonPath("$.count", is(4)))
        ;
    }
}