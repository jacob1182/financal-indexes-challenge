package com.example.financialindexes.domain;

import lombok.Value;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

import static com.example.financialindexes.TickUtils.genTick;
import static org.assertj.core.api.Assertions.assertThat;

class StatisticsSnapshotTest {

    Random random = new Random();

    @Test
    void testPerformanceWhenAddingNewTick() {
        StatisticsSnapshot snapshot = StatisticsSnapshot.EMPTY;
        var timeSum = 0;
        for (int i = 0; i < 10_000; i++) {
            var tick = genTick(random.nextInt(59));
            var time = System.currentTimeMillis();
            snapshot = snapshot.withTick(tick);
            timeSum += System.currentTimeMillis() - time;
        }
        assertThat(timeSum).isLessThan(50);
    }


    @Test
    void testStatisticsValues() {
        StatisticsSnapshot snapshot = StatisticsSnapshot.EMPTY;
        var ticks = new TreeSet<Tick>();
        for (int i = 0; i < 10; i++) {
            var price = (double) (random.nextInt(100) + 100) / 100;
            var timestamp = System.currentTimeMillis() - random.nextInt(59) * 1_000;
            var tick = genTick(price, timestamp);
            ticks.add(tick);
            snapshot = snapshot.withTick(tick);
            assertStatistic(snapshot.getStatistics(), ticks);
        }
    }

    private void assertStatistic(Statistics statistics, Collection<Tick> ticks) {
        var minPrice = new BigDecimal(Double.MAX_VALUE);
        var maxPrice = new BigDecimal(Double.MIN_VALUE);
        var sumPrice = BigDecimal.ZERO;
        var count = 0L;

        for (Tick current: ticks) {
            minPrice = minPrice.min(current.getPrice());
            maxPrice = maxPrice.max(current.getPrice());
            sumPrice = sumPrice.add(current.getPrice());
            count++;
        }

        assertThat(statistics)
                .extracting("min", "max", "sum", "count")
                .containsExactly(minPrice, maxPrice, sumPrice, count);
    }

    @Value(staticConstructor = "of")
    public static class StatisticsSnapshot {
        Statistics statistics;
        TreeSet<Tick> source;

        public static StatisticsSnapshot EMPTY = of(Statistics.EMPTY, new TreeSet<>());

        public StatisticsSnapshot withTick(Tick tick) {
            if (tick == null || !tick.isFresh())
                return this;

            source.add(tick); // O(log(n))

            // remove old ticks & old price sum
            var it = source.iterator();
            var sumOldPrice = BigDecimal.ZERO;
            Tick currentTick;

            while (it.hasNext() && !(currentTick = it.next()).isFresh()) { // O(n)
                sumOldPrice = sumOldPrice.add(currentTick.getPrice());
                it.remove();
            }

            // adding new tick modify affect statistics values
            var minPrice = statistics.getMin().min(tick.getPrice());
            var maxPrice = statistics.getMax().max(tick.getPrice());
            var sumPrice = statistics.getSum().add(tick.getPrice()).subtract(sumOldPrice);
            var stat = Statistics.of(minPrice, maxPrice, sumPrice, statistics.getCount() + 1);

            return of(stat, source);
        }
    }
}