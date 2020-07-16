package com.example.financialindexes.domain;

import com.example.financialindexes.utils.MultiplePriorityTree;
import lombok.Value;

import java.math.BigDecimal;
import java.util.function.BiFunction;

import static com.example.financialindexes.domain.Statistics.*;

@Value(staticConstructor = "of")
public class StatisticsSnapshot {
    Statistics statistics;
    MultiplePriorityTree<Tick> source;
    long startTimestamp;

    public static StatisticsSnapshot getNewInstance() {
        return of(Statistics.EMPTY, MultiplePriorityTree.of(SORT_BY_PRICE, SORT_BY_TIME));
    }

    private static StatisticsSnapshot of(Statistics stat, MultiplePriorityTree<Tick> source) {
        var startTimestamp = source.isEmpty() ? 0 : source.first(SORT_BY_TIME_KEY).getTimestamp();
        return of(stat, source, startTimestamp);
    }

    public boolean isFresh(long currentTimestamp) {
        return !source.isEmpty() && currentTimestamp - 60_000 <= startTimestamp;
    }

    public StatisticsSnapshot withTick(long currentTimestamp, Tick tick) {

        if (tick == null || !tick.isFresh(currentTimestamp))
            return this;

        source.add(tick); // O(log(n))

        var isFresh = isFresh(currentTimestamp);
        var isEdge = statistics.isEdge(tick);

        // remove old ticks & old price sum
        var stat = removeOldTicks(currentTimestamp, (sumOldPrice, recalculate) -> {
            // adding new tick modify affect statistics values
            return !recalculate && (isFresh || isEdge)
                    ? statistics.withTick(tick, sumOldPrice, source.size())
                    : statistics.calculate(sumOldPrice.subtract(tick.getPrice()), getSource());
        });

        return of(stat, source);
    }

    public StatisticsSnapshot recalculate(long time) {
        if (source.isEmpty() || isFresh(time))
            return this;

        var stat = removeOldTicks(time, (sumOldPrice, recalculate) -> recalculate
                ? statistics.calculate(sumOldPrice, source) // O(1)
                : Statistics.of( // O(1)
                    statistics.getMin(),
                    statistics.getMax(),
                    statistics.getSum().subtract(sumOldPrice),
                    source.size()
        ));

        return of(stat, source);
    }

    private Statistics removeOldTicks(long time, BiFunction<BigDecimal, Boolean, Statistics> then) {
        var sumOldPrice = BigDecimal.ZERO;
        var forceRecalculation = false;
        Tick current;

        while (!source.isEmpty() && !(current = source.first(SORT_BY_TIME_KEY) /* O(1) */).isFresh(time)) { // O(n*log(n))
            sumOldPrice = sumOldPrice.add(current.getPrice());
            forceRecalculation |= statistics.isEdge(current);
            source.pollFirst(SORT_BY_TIME_KEY); // O(log(n))
        }

        return then.apply(sumOldPrice, forceRecalculation);
    }
}