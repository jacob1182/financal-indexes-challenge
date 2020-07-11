package com.example.financialindexes.domain;

import com.example.financialindexes.utils.MultiplePriorityTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static com.example.financialindexes.IndexUtils.genTick;
import static com.example.financialindexes.domain.Statistics.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {

    private MultiplePriorityTree<Tick> source;
    private List<Tick> ticks;

    @BeforeEach
    void setUp() {
        source = MultiplePriorityTree.of(SORT_BY_PRICE, SORT_BY_TIME);
        var timestamp = System.currentTimeMillis();
        ticks = List.of(
                genTick(100, timestamp - 50_000),
                genTick(150, timestamp - 40_000),
                genTick(250, timestamp - 30_000),
                genTick(200, timestamp - 20_000)
        );
    }

    @Test
    void shouldCalculateStatistics() {

        Statistics stats = withTicks();

        var statistics = stats.calculate(BigDecimal.ZERO, source);

        assertEquals(100d, statistics.getMin().doubleValue());
        assertEquals(250d, statistics.getMax().doubleValue());
        assertEquals(700d, statistics.getSum().doubleValue());
        assertEquals(700d/4, statistics.getAvg().doubleValue());
        assertEquals(4, statistics.getCount());
    }

    @Test
    void shouldCalculateStatisticsWhenRemovingTicks() {

        Statistics stats = withTicks();
        var deleted = source.pollFirst(SORT_BY_TIME_KEY);

        var statistics = stats.calculate(deleted.getPrice(), source);

        assertEquals(150d, statistics.getMin().doubleValue());
        assertEquals(250d, statistics.getMax().doubleValue());
        assertEquals(600d, statistics.getSum().doubleValue());
        assertEquals(600d/3, statistics.getAvg().doubleValue());
        assertEquals(3, statistics.getCount());
    }

    @Test
    void shouldCalculateStatisticsWithEmptySource() {
        var statistics = Statistics.EMPTY.calculate(BigDecimal.ZERO, source);

        assertEquals(Double.MAX_VALUE, statistics.getMin().doubleValue());
        assertEquals(Double.MIN_VALUE, statistics.getMax().doubleValue());
        assertEquals(0, statistics.getSum().doubleValue());
        assertEquals(0, statistics.getAvg().doubleValue());
        assertEquals(0, statistics.getCount());
    }

    private Statistics withTicks() {
        var stats = Statistics.EMPTY;
        for (Tick tick : ticks) {
            source.add(tick);
            stats = stats.withTick(tick, BigDecimal.ZERO, source.size());
        }
        return stats;
    }
}