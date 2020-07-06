package com.example.financialindexes.domain;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.TreeSet;

import static com.example.financialindexes.IndexUtils.assertStatistic;
import static com.example.financialindexes.IndexUtils.genTick;
import static java.util.Comparator.comparing;
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
            snapshot = snapshot.withTick(time, tick);
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
            snapshot = snapshot.withTick(System.currentTimeMillis(), tick);
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
            snapshot = snapshot.withTick(System.currentTimeMillis(), tick);
        }

        assertStatistic(snapshot, ticks);

        Thread.sleep(150);

        var tick = genTick(200, System.currentTimeMillis());
        ticks.add(tick);

        snapshot = snapshot.withTick(System.currentTimeMillis(), tick);
        assertStatistic(snapshot, ticks);
    }

    @Test
    void testPerformanceWhenAsyncRecalculate() throws InterruptedException {
        var snapshot = StatisticsSnapshot.getNewInstance();
        var ticks = new TreeSet<>(comparing(Tick::getTimestamp));

        // add 100 ticks with a window of 10ms
        var startTime = System.currentTimeMillis();
        var timestamp = startTime - 60_000;
        for (int i = 0; i < 100; i++) {
            var price = (double) (random.nextInt(100) + 100) / 100;
            var tick = genTick(price, timestamp + 10 * i);
            snapshot = snapshot.withTick(System.currentTimeMillis(), tick);
            ticks.add(tick);
        }

        // test performance of recalculate every 100ms
        var timeSum = 0;
        while (snapshot.getStatistics().getCount() > 0) {
            var current = System.currentTimeMillis();
            var time = System.currentTimeMillis();
            snapshot = snapshot.recalculate(current);
            timeSum += System.currentTimeMillis() - time;
            assertStatistic(snapshot, ticks);
            Thread.sleep(100);
        }

        assertThat(timeSum).isLessThanOrEqualTo(5);
    }

    @Test
    void shouldReturnSameStatisticsWhenAddingNullTick() {
        var snapshot1 = StatisticsSnapshot.getNewInstance();
        var snapshot2 = snapshot1.withTick(0, null);

        assertThat(snapshot1).isEqualTo(snapshot2);
    }

    @Test
    void shouldReturnSameStatisticsWhenAddingOldTick() {
        var snapshot1 = StatisticsSnapshot.getNewInstance();
        var snapshot2 = snapshot1.withTick(System.currentTimeMillis(), genTick(61));

        assertThat(snapshot1).isEqualTo(snapshot2);
    }
}