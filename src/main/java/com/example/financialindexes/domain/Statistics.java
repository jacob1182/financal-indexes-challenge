package com.example.financialindexes.domain;


import lombok.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Value(staticConstructor = "of")
public class Statistics {
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
