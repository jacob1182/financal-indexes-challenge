package com.example.financialindexes.domain;

import lombok.Value;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;
import java.util.TreeSet;

import static com.example.financialindexes.TickUtils.genTick;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

class StatisticsSnapshotTest {

    Random random = new Random();

    @Test
    void testPerformanceWhenAddingNewTick() {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var timeSum = 0;
        var timestamp = System.currentTimeMillis() - 60_000;
        for (int i = 0; i < 10_000; i++) {
            var price = (double) (random.nextInt(100) + 100) / 100;
            var tick = genTick(price, timestamp + i);
            var time = System.currentTimeMillis();
            snapshot = snapshot.withTick(tick);
            timeSum += System.currentTimeMillis() - time;
        }
        assertThat(timeSum).isLessThanOrEqualTo(150);
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
            assertStatistic(snapshot, ticks);
        }
    }

    @Test
    void testRaceConditionWhenRemoveOldTicksAndPriceIsNotStatisticsEdgeValue() throws InterruptedException {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var ticks = new TreeSet<>(comparing(Tick::getTimestamp));

        var timestamp = System.currentTimeMillis() - 59_900;
        for (int i = 0; i < 100; i++) {
            var price = 100d + random.nextInt(100);
            var tick = genTick(price, timestamp + i * 2);
            ticks.add(tick);
            snapshot = snapshot.withTick(tick);
        }

        assertStatistic(snapshot, ticks);

        Thread.sleep(150);

        var tick = genTick(200, System.currentTimeMillis());
        ticks.add(tick);

        snapshot = snapshot.withTick(tick);
        assertStatistic(snapshot, ticks);
    }

    private void assertStatistic(StatisticsSnapshot snapshot, Collection<Tick> ticks) {
        var time = System.currentTimeMillis();
        var threshold = time - 60_000;
        if (!snapshot.isFresh(time)) {
            var startTimestamp = snapshot.getSource().first().getTimestamp();
            assertThat(threshold - startTimestamp).isLessThanOrEqualTo(2); // allow diff time error up to 2ms
            threshold = startTimestamp;
        }

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

        assertThat(snapshot.getStatistics())
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

        public boolean isFresh(long currentTimestamp) {
            return !source.isEmpty() && source.first().isFresh(currentTimestamp);
        }

        public StatisticsSnapshot withTick(Tick tick) {
            var time = System.currentTimeMillis();

            if (tick == null || !tick.isFresh(time))
                return this;

            source.add(tick); // O(log(n))

            var isFresh = isFresh(time);
            var isEdge = statistics.isEdge(tick);

            // remove old ticks & old price sum
            var sumOldPrice = BigDecimal.ZERO;
            var needsRecalculation = false;
            var it = source.iterator();
            Tick current;

            while (it.hasNext() && !(current = it.next()).isFresh(time)) { // O(n)
                sumOldPrice = sumOldPrice.add(current.getPrice());
                needsRecalculation |= statistics.isEdge(current);
                it.remove();
            }

            // adding new tick modify affect statistics values
            var stat = !needsRecalculation && (isFresh || isEdge)
                    ? statistics.withTick(tick, sumOldPrice, source.size())
                    : Statistics.calculate(source);

            return of(stat, source);
        }
    }
}