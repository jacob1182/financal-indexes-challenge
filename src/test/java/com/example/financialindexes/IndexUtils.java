package com.example.financialindexes;

import com.example.financialindexes.domain.StatisticsSnapshot;
import com.example.financialindexes.domain.Tick;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexUtils {

    public static List<String> instruments = List.of("AAA", "BBB", "CCC", "DDD", "EEE", "FFF", "GGG", "HHH");
    private static Random random = new Random();

    public static Tick genTick(long secondsOlder) {
        return genTick(143.82d, System.currentTimeMillis() - secondsOlder * 1000);
    }

    public static Tick genTick(double price, long timestamp) {
        return new Tick(instruments.get(random.nextInt(instruments.size())), BigDecimal.valueOf(price), timestamp);
    }

    public static void assertStatistic(StatisticsSnapshot snapshot, Collection<Tick> ticks) {
        var time = System.currentTimeMillis();
        var threshold = time - 60_000;
        var stat = snapshot.getStatistics();

        if (!snapshot.isFresh(time) && snapshot.getStartTimestamp() > 0) {
            var startTimestamp = snapshot.getStartTimestamp();
            assertTrue(threshold - startTimestamp < 2); // allow diff time error up to 2ms
            threshold = startTimestamp;
        }

        var minPrice = new BigDecimal(Double.MAX_VALUE);
        var maxPrice = new BigDecimal(Double.MIN_VALUE);
        var sumPrice = BigDecimal.ZERO;
        var count = 0L;

        for (Tick current : ticks) {
            if (current.getTimestamp() >= threshold) {
                minPrice = minPrice.min(current.getPrice());
                maxPrice = maxPrice.max(current.getPrice());
                sumPrice = sumPrice.add(current.getPrice());
                count++;
            }
        }

        assertEquals(stat.getMin().doubleValue(), minPrice.doubleValue());
        assertEquals(stat.getMax().doubleValue(), maxPrice.doubleValue());
        assertEquals(stat.getSum().doubleValue(), sumPrice.doubleValue());
        assertEquals(stat.getCount(), count);
    }
}
