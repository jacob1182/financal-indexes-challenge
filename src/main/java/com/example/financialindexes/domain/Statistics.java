package com.example.financialindexes.domain;


import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

@EqualsAndHashCode
@Value(staticConstructor = "of")
public class Statistics {

    public static final Statistics EMPTY =   of(
            BigDecimal.valueOf(Double.MAX_VALUE),
            BigDecimal.valueOf(Double.MIN_VALUE),
            BigDecimal.ZERO, 0);

    BigDecimal min;
    BigDecimal max;
    BigDecimal sum;
    long count;

    public static Statistics calculate(Collection<Tick> freshTicks) {
        var minPrice = BigDecimal.valueOf(Double.MAX_VALUE);
        var maxPrice = BigDecimal.valueOf(Double.MIN_VALUE);
        var sumPrice = BigDecimal.ZERO;

        for (Tick current : freshTicks) {
            minPrice = minPrice.min(current.getPrice());
            maxPrice = maxPrice.max(current.getPrice());
            sumPrice = sumPrice.add(current.getPrice());
        }

        return of(minPrice, maxPrice, sumPrice, freshTicks.size());
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
