package com.example.financialindexes.domain;


import com.example.financialindexes.utils.MultiplePriorityTree;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Map;

import static java.util.Comparator.comparing;

@EqualsAndHashCode
@Value(staticConstructor = "of")
public class Statistics {

    public static final String SORT_BY_TIME_KEY = "time";
    public static final String SORT_BY_PRICE_KEY = "price";

    public static final Map.Entry<String, Comparator<Tick>> SORT_BY_TIME = Map.entry(SORT_BY_TIME_KEY,
            comparing(Tick::getTimestamp).thenComparing(Tick::getInstrument));
    public static final Map.Entry<String, Comparator<Tick>> SORT_BY_PRICE = Map.entry(SORT_BY_PRICE_KEY,
            comparing(Tick::getPrice).thenComparing(Tick::getTimestamp).thenComparing(Tick::getInstrument));

    public static final Statistics EMPTY =   of(
            BigDecimal.valueOf(Double.MAX_VALUE),
            BigDecimal.valueOf(Double.MIN_VALUE),
            BigDecimal.ZERO, 0);

    BigDecimal min;
    BigDecimal max;
    BigDecimal sum;
    long count;

    public Statistics calculate(BigDecimal sumOldPrice, MultiplePriorityTree<Tick> source) {


        return source.isEmpty() ? Statistics.EMPTY // O(1)
                : Statistics.of(
                    source.first(SORT_BY_PRICE_KEY).getPrice(),// O(1)
                    source.last(SORT_BY_PRICE_KEY).getPrice(), // O(1)
                    sum.add(sumOldPrice.negate()), // O(1)
                    source.size() // O(1)
                );
    }

    public BigDecimal getAvg() {
        return count == 0
                ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(count), RoundingMode.HALF_EVEN);
    }

    public boolean isEdge(Tick tick) {
        return tick.getPrice().compareTo(min) <= 0
            || tick.getPrice().compareTo(max) >= 0;
    }

    public Statistics withTick(Tick tick, BigDecimal oldPrice, long count) {
        var minPrice = min.min(tick.getPrice());
        var maxPrice = max.max(tick.getPrice());
        var sumPrice = sum.add(tick.getPrice()).subtract(oldPrice);

        return of(minPrice, maxPrice, sumPrice, count);
    }
}
