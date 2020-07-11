package com.example.financialindexes.experimental;

import com.example.financialindexes.domain.Tick;
import com.example.financialindexes.utils.MultiplePriorityTree;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiplePriorityTreeTest {

    @Test
    void testRedBlackTree() {

        Random random = new Random();
        BiFunction<Double, Long, Tick> genTick = (price, timeStamp) -> {
            String[] instruments = {"AB", "CD", "EF"};
            return new Tick(
                    instruments[random.nextInt(instruments.length)],
                    BigDecimal.valueOf(price),
                    timeStamp
            );
        };

        var TIME = "time";
        var PRICE = "price";

        var ticks = new MultiplePriorityTree<>(
                Map.of(
                        TIME, comparing(Tick::getTimestamp),
                        PRICE, comparing(Tick::getPrice)
                                .thenComparing(Tick::getTimestamp)
                                .thenComparing(Tick::getInstrument)
                )
        );

        ticks.add(genTick.apply(300d, 10L));
        ticks.add(genTick.apply(200d, 60L));
        ticks.add(genTick.apply(600d, 50L));
        ticks.add(genTick.apply(700d, 30L));
        ticks.add(genTick.apply(500d, 40L));
        ticks.add(genTick.apply(400d, 20L));

        ticks.prettyPrint();
        assertEquals(300d, ticks.first(TIME).getPrice().doubleValue());
        assertEquals(200d, ticks.last(TIME).getPrice().doubleValue());
        assertEquals(200d, ticks.first(PRICE).getPrice().doubleValue());
        assertEquals(700d, ticks.last(PRICE).getPrice().doubleValue());

        assertEquals(200d, ticks.pollFirst(PRICE).getPrice().doubleValue());
        ticks.prettyPrint();

        assertEquals(300d, ticks.first(PRICE).getPrice().doubleValue());
        assertEquals(700d, ticks.last(PRICE).getPrice().doubleValue());
        assertEquals(300d, ticks.first(TIME).getPrice().doubleValue());
        assertEquals(600d, ticks.last(TIME).getPrice().doubleValue());

        assertEquals(300d, ticks.pollFirst(TIME).getPrice().doubleValue());
        ticks.prettyPrint();
        assertEquals(400d, ticks.first(PRICE).getPrice().doubleValue());
        assertEquals(700d, ticks.last(PRICE).getPrice().doubleValue());
        assertEquals(400d, ticks.first(TIME).getPrice().doubleValue());
        assertEquals(600d, ticks.last(TIME).getPrice().doubleValue());

        assertEquals(400d, ticks.pollFirst(PRICE).getPrice().doubleValue());
        ticks.prettyPrint();
        assertEquals(500d, ticks.first(PRICE).getPrice().doubleValue());
        assertEquals(700d, ticks.last(PRICE).getPrice().doubleValue());
        assertEquals(700d, ticks.first(TIME).getPrice().doubleValue());
        assertEquals(600d, ticks.last(TIME).getPrice().doubleValue());

        assertEquals(700d, ticks.pollFirst(TIME).getPrice().doubleValue());
        ticks.prettyPrint();
        assertEquals(500d, ticks.first(PRICE).getPrice().doubleValue());
        assertEquals(600d, ticks.last(PRICE).getPrice().doubleValue());
        assertEquals(500d, ticks.first(TIME).getPrice().doubleValue());
        assertEquals(600d, ticks.last(TIME).getPrice().doubleValue());

    }
}
