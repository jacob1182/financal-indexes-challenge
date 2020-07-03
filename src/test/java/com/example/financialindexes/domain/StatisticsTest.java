package com.example.financialindexes.domain;

import com.example.financialindexes.TickUtils;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatisticsTest {

    @Test
    void shouldCalculateStatistics() {

        var timestamp = System.currentTimeMillis();
        var source = List.of(
                TickUtils.genTick(325, timestamp - 61_000),
                TickUtils.genTick(100, timestamp - 50_000),
                TickUtils.genTick(150, timestamp - 40_000),
                TickUtils.genTick(250, timestamp - 30_000),
                TickUtils.genTick(200, timestamp - 20_000)
        );

        var statistics = Statistics.calculate(source, timestamp - 60_000);

        assertEquals(100d, statistics.getMin().doubleValue());
        assertEquals(250d, statistics.getMax().doubleValue());
        assertEquals(700d, statistics.getSum().doubleValue());
        assertEquals(700d/4, statistics.getAvg().doubleValue());
        assertEquals(4, statistics.getCount());

    }

    @Test
    void shouldCalculateStatisticsWithEmptySource() {
        var statistics = Statistics.calculate(List.of(), System.currentTimeMillis());

        assertEquals(Double.MAX_VALUE, statistics.getMin().doubleValue());
        assertEquals(Double.MIN_VALUE, statistics.getMax().doubleValue());
        assertEquals(0, statistics.getSum().doubleValue());
        assertEquals(0, statistics.getAvg().doubleValue());
        assertEquals(0, statistics.getCount());
    }

    @Value(staticConstructor = "of")
    public static class Statistics {
        BigDecimal min;
        BigDecimal max;
        BigDecimal sum;
        long count;

        public static Statistics calculate(List<Tick> source, long startTimestamp) {
            var minPrice = BigDecimal.valueOf(Double.MAX_VALUE);
            var maxPrice = BigDecimal.valueOf(Double.MIN_VALUE);
            var sumPrice = BigDecimal.ZERO;
            var counter = 0;

            for (Tick current : source) {
                if (current.getTimestamp() >= startTimestamp) {
                    minPrice = minPrice.min(current.getPrice());
                    maxPrice = maxPrice.max(current.getPrice());
                    sumPrice = sumPrice.add(current.getPrice());
                    counter++;
                }
            }

            return of(minPrice, maxPrice, sumPrice, counter);
        }

        public BigDecimal getAvg() {
            return count == 0
                    ? BigDecimal.ZERO
                    : sum.divide(BigDecimal.valueOf(count), RoundingMode.HALF_EVEN);
        }
    }
}