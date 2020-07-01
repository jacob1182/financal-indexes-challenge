package com.example.financialindexes.domain;

import lombok.Value;

import java.math.BigDecimal;

@Value
public class Tick {
    String instrument;
    BigDecimal price;
    Long timestamp;
}
