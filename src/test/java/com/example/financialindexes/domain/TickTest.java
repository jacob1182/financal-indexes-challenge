package com.example.financialindexes.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TickTest {

    @Test
    void verifyWhetherTickIsFresh() {
        var freshTick = genTick(59);
        assertTrue(freshTick.isFresh());

        var oldTick = genTick(60);
        assertFalse(oldTick.isFresh());
    }

    private Tick genTick(int secondsOlder) {
        return new Tick("ANY", BigDecimal.ONE, System.currentTimeMillis() - secondsOlder * 1000);
    }
}