package com.example.financialindexes.api;

import com.example.financialindexes.app.IndexApplicationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.financialindexes.IndexUtils.genTick;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private IndexApplicationService applicationService;

    @AfterEach
    void cleanUp() {
        applicationService.clearTicks();
    }

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

    @Test
    public void retrieveEmptyStatistics() throws Exception {

        mvc.perform(get("/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min", is(0d)))
                .andExpect(jsonPath("$.max", is(0d)))
                .andExpect(jsonPath("$.avg", is(0)))
                .andExpect(jsonPath("$.count", is(0)))
        ;
    }

    @Test
    public void retrieveStatisticsPerInstrument() throws Exception {

        var timestamp = System.currentTimeMillis();
        List.of(
                genTick("AAA", 325, timestamp - 61_000),
                genTick("BBB",100, timestamp - 50_000),
                genTick("AAA",150, timestamp - 40_000),
                genTick("BBB",250, timestamp - 30_000),
                genTick("AAA",200, timestamp - 20_000)
        ).forEach(applicationService::receiveTick);

        mvc.perform(get("/statistics/AAA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.min", is(150d)))
                .andExpect(jsonPath("$.max", is(200d)))
                .andExpect(jsonPath("$.avg", is(350d/2)))
                .andExpect(jsonPath("$.count", is(2)))
        ;
    }
}