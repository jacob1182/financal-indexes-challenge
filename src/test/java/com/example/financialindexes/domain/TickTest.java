package com.example.financialindexes.domain;

import org.junit.jupiter.api.Test;

import static com.example.financialindexes.TickUtils.genTick;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TickTest {

    @Test
    void verifyWhetherTickIsFresh() {
        var freshTick = genTick(59);
        assertTrue(freshTick.isFresh());

        var oldTick = genTick(60);
        assertFalse(oldTick.isFresh());
    }
}