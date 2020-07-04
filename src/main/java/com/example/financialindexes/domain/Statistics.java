package com.example.financialindexes.domain;


import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    public static Statistics calculate(List<Tick> freshTicks) {
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
}
