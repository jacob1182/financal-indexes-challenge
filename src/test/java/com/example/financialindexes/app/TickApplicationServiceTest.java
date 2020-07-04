package com.example.financialindexes.app;

import com.example.financialindexes.domain.TickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.IntStream;

import static com.example.financialindexes.TickUtils.genTick;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TickApplicationServiceTest {

    private TickApplicationService applicationService;
    private TickRepository tickRepository;

    @BeforeEach
    void setUp() {
        tickRepository = new TickRepository();
        applicationService = new TickApplicationService(tickRepository);
    }

    Random random = new Random();

    /**
     * Old ticks should not impact in the performance of adding new ticks
     * */
    @Test
    void testPerformanceImpactOfLargeAmountOfOldTicksPersisted() {
        IntStream.range(0, 100_000)
                .forEach(__ -> tickRepository.save(genTick(60 + random.nextInt(10_000))));

        var tick = genTick(150, System.currentTimeMillis());
        var time = System.currentTimeMillis();
        applicationService.receiveTick(tick);
        assertEquals(0, System.currentTimeMillis() - time);
    }
}