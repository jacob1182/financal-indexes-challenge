package com.example.financialindexes;

import com.example.financialindexes.domain.Tick;

import java.math.BigDecimal;

public class TickUtils {

    public static Tick genTick(long secondsOlder) {
        return genTick(143.82d, System.currentTimeMillis() - secondsOlder * 1000);
    }

    public static Tick genTick(double price, long timestamp) {
        return new Tick("IBM.N", BigDecimal.valueOf(price), timestamp);
    }
}
