package com.example.financialindexes.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.financialindexes.IndexUtils.genTick;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {

    @Test
    void shouldCalculateStatistics() {

        var timestamp = System.currentTimeMillis();
        var freshTicks = List.of(
                genTick(100, timestamp - 50_000),
                genTick(150, timestamp - 40_000),
                genTick(250, timestamp - 30_000),
                genTick(200, timestamp - 20_000)
        );

        var statistics = Statistics.calculate(freshTicks);

        assertEquals(100d, statistics.getMin().doubleValue());
        assertEquals(250d, statistics.getMax().doubleValue());
        assertEquals(700d, statistics.getSum().doubleValue());
        assertEquals(700d/4, statistics.getAvg().doubleValue());
        assertEquals(4, statistics.getCount());

    }

    @Test
    void shouldCalculateStatisticsWithEmptySource() {
        var statistics = Statistics.calculate(List.of());

        assertEquals(Double.MAX_VALUE, statistics.getMin().doubleValue());
        assertEquals(Double.MIN_VALUE, statistics.getMax().doubleValue());
        assertEquals(0, statistics.getSum().doubleValue());
        assertEquals(0, statistics.getAvg().doubleValue());
        assertEquals(0, statistics.getCount());
    }
}