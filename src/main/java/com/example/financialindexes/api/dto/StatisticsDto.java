package com.example.financialindexes.api.dto;

import com.example.financialindexes.domain.Statistics;
import lombok.Value;

import java.math.BigDecimal;

@Value(staticConstructor = "of")
public class StatisticsDto {
    BigDecimal min;
    BigDecimal max;
    BigDecimal avg;
    long count;

    public static StatisticsDto from(Statistics statistics) {
        var min = statistics.getMin().doubleValue();
        var max = statistics.getMax().doubleValue();
        return of(
                BigDecimal.valueOf(min == Double.MAX_VALUE ? 0 : min),
                BigDecimal.valueOf(max == Double.MIN_VALUE ? 0 : max),
                statistics.getAvg(),
                statistics.getCount());
    }
}
