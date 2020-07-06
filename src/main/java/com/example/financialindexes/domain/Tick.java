package com.example.financialindexes.domain;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.math.BigDecimal;

@Value
@EqualsAndHashCode
public class Tick {
    String instrument;
    BigDecimal price;
    Long timestamp;

    /**
     * Returns whether the tick timestamp is not older than 60 seconds
     */
    public boolean isFresh(long currentTimestamp) {
        return currentTimestamp - 60_000 <= timestamp;
    }
}
