package com.example.financialindexes.experimental;

import com.example.financialindexes.domain.Tick;
import com.example.financialindexes.utils.MultiplePriorityTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiplePriorityTreeTest {

    private MultiplePriorityTree<Tick> ticks;
    private final String TIME = "time";
    private final String PRICE = "price";

    @BeforeEach
    void setUp() {
        Random random = new Random();
        BiFunction<Double, Long, Tick> genTick = (price, timeStamp) -> {
            String[] instruments = {"AB", "CD", "EF"};
            return new Tick(
                    instruments[random.nextInt(instruments.length)],
                    BigDecimal.valueOf(price),
                    timeStamp
            );
        };

        ticks = new MultiplePriorityTree<>(
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
    }

    @Test
    void testRedBlackTree() {

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

    @Test
    void testRemoveIssue() {
        var random = new Random();
        for (int i = 15; i < 20; i++) {
            var allTicks = Stream.generate(() ->
                    new Tick("", BigDecimal.valueOf(100 + random.nextInt(100)), 10L + + random.nextInt(100)))
                    .limit(i)
                    .peek(System.out::println)
                    .collect(Collectors.toList());
            allTicks.add(new Tick("", BigDecimal.valueOf(1000), 100L));

            ticks.clear();
            allTicks.forEach(tick -> {
                ticks.add(tick);

                assertEquals(ticks.first(PRICE), ticks.minimum(PRICE));
                assertEquals(ticks.first(TIME), ticks.minimum(TIME));
                assertEquals(ticks.last(PRICE), ticks.maximum(PRICE));
                assertEquals(ticks.last(TIME), ticks.maximum(TIME));
            });

            allTicks.forEach(tick -> {
                ticks.pollFirst(PRICE);

                if (!ticks.isEmpty()) {

                    assertEquals(ticks.first(PRICE), ticks.minimum(PRICE));
                    assertEquals(ticks.first(TIME), ticks.minimum(TIME));
                    assertEquals(ticks.last(PRICE), ticks.maximum(PRICE));
                    assertEquals(ticks.last(TIME), ticks.maximum(TIME));
                }
            });
        }
    }
}
