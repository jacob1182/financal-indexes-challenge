package com.example.financialindexes.domain;

import com.example.financialindexes.TickUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TickRepositoryTest {

    @Test
    void saveTick() {
        var repository = new TickRepository();
        var tick = TickUtils.genTick(0);

        repository.save(tick);

        assertEquals(tick, repository.findByTimestamp(tick.getTimestamp()));
    }
}