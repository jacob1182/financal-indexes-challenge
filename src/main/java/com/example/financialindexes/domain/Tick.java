package com.example.financialindexes.domain;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Tick {
    String instrument;
    BigDecimal price;
    Long timestamp;

    /**
     * Returns whether the tick timestamp is not older than 60 seconds
     * */
    public boolean isFresh() {
        return System.currentTimeMillis() - 60000 < timestamp;
    }
}
