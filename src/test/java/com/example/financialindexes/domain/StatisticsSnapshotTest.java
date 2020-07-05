package com.example.financialindexes.domain;

import lombok.Value;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

import static com.example.financialindexes.TickUtils.genTick;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;

class StatisticsSnapshotTest {

    Random random = new Random();

    @Test
    void testPerformanceWhenAddingNewTick() {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var timeSum = 0;
        for (int i = 0; i < 10_000; i++) {
            var tick = genTick(random.nextInt(59));
            var time = System.currentTimeMillis();
            snapshot = snapshot.withTick(tick);
            timeSum += System.currentTimeMillis() - time;
        }
        assertThat(timeSum).isLessThanOrEqualTo(50);
    }


    @Test
    void testStatisticsValues() {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var ticks = new TreeSet<>(comparing(Tick::getTimestamp));
        for (int i = 0; i < 10; i++) {
            var price = (double) (random.nextInt(100) + 100) / 100;
            var timestamp = System.currentTimeMillis() - i * 1_000;
            var tick = genTick(price, timestamp);
            ticks.add(tick);
            snapshot = snapshot.withTick(tick);
            assertStatistic(snapshot.getStatistics(), ticks);
        }
    }

    @Test
    void testRaceConditionWhenRemoveOldTicksAndPriceIsNotStatisticsEdgeValue() throws InterruptedException {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var ticks = new TreeSet<>(comparing(Tick::getTimestamp));

        var timestamp = System.currentTimeMillis() - 59_900;
        for (int i = 0; i < 100; i++) {
            var price = 10d + random.nextInt(100);
            var tick = genTick(price, timestamp + i * 2);
            ticks.add(tick);
            snapshot = snapshot.withTick(tick);
        }

        assertStatistic(snapshot.getStatistics(), ticks);

        Thread.sleep(150);

        var tick = genTick(200, System.currentTimeMillis());
        ticks.add(tick);

        snapshot = snapshot.withTick(tick);
        assertStatistic(snapshot.getStatistics(), ticks);
    }

    private void assertStatistic(Statistics statistics, Collection<Tick> ticks) {
        var threshold = System.currentTimeMillis() - 60_000;
        var minPrice = new BigDecimal(Double.MAX_VALUE);
        var maxPrice = new BigDecimal(Double.MIN_VALUE);
        var sumPrice = BigDecimal.ZERO;
        var count = 0L;

        for (Tick current : ticks) {
            if (current.getTimestamp() >= threshold) {
                minPrice = minPrice.min(current.getPrice());
                maxPrice = maxPrice.max(current.getPrice());
                sumPrice = sumPrice.add(current.getPrice());
                count++;
            }
        }

        assertThat(statistics)
                .extracting("min", "max", "sum", "count")
                .containsExactly(minPrice, maxPrice, sumPrice, count);
    }

    @Value(staticConstructor = "of")
    public static class StatisticsSnapshot {
        Statistics statistics;
        TreeSet<Tick> source;

        public static StatisticsSnapshot getNewInstance() {
            return of(Statistics.EMPTY, new TreeSet<>(comparing(Tick::getTimestamp)));
        }

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