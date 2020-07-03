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
        return of(statistics.getMin(), statistics.getMax(), statistics.getAvg(), statistics.getCount());
    }
}
