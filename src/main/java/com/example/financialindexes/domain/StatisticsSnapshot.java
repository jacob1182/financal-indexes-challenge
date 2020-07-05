package com.example.financialindexes.domain;

import lombok.Value;

import java.math.BigDecimal;
import java.util.TreeSet;
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;

@Value(staticConstructor = "of")
public class StatisticsSnapshot {
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
        var stat = removeOldTicks(time, (sumOldPrice, recalculate) -> {
            // adding new tick modify affect statistics values
            return !recalculate && (isFresh || isEdge)
                    ? statistics.withTick(tick, sumOldPrice, source.size())
                    : Statistics.calculate(source);
        });

        return of(stat, source);
    }

    public StatisticsSnapshot recalculate(long time) {
        if(source.isEmpty() || isFresh(time))
            return this;

        var stat =  removeOldTicks(time, (sumOldPrice, recalculate) -> recalculate
                        ? Statistics.calculate(source)
                        : Statistics.of(statistics.getMin(),
                            statistics.getMax(),
                            statistics.getSum().subtract(sumOldPrice),
                            source.size()));

        return of(stat, source);
    }

    private Statistics removeOldTicks(long time, BiFunction<BigDecimal, Boolean, Statistics> then) {
        var sumOldPrice = BigDecimal.ZERO;
        var forceRecalculation = false;
        var it = source.iterator();
        Tick current;

        while (it.hasNext() && !(current = it.next()).isFresh(time)) { // O(n)
            sumOldPrice = sumOldPrice.add(current.getPrice());
            forceRecalculation |= statistics.isEdge(current);
            it.remove();
        }

        return then.apply(sumOldPrice, forceRecalculation);
    }
}