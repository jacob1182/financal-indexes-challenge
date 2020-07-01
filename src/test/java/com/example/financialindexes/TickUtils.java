package com.example.financialindexes;

import com.example.financialindexes.domain.Tick;

import java.math.BigDecimal;

public class TickUtils {
    public static Tick genTick(long secondsOlder) {
        return new Tick("IBM.N", BigDecimal.valueOf(143.82), System.currentTimeMillis() - secondsOlder * 1000);
    }
}
