package com.example.financialindexes.app;

import com.example.financialindexes.domain.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

import static com.example.financialindexes.IndexUtils.assertStatistic;
import static com.example.financialindexes.IndexUtils.genTick;
import static java.util.Collections.synchronizedList;

class IndexApplicationServiceTest {

    private IndexApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new IndexApplicationService();
    }

    Random random = new Random();

    @Test
    void testConcurrentStatisticsCalculation() throws InterruptedException {
        var time = System.currentTimeMillis() - 60_000;
        var ticks = synchronizedList(new ArrayList<Tick>());
        var latch = new CountDownLatch(5);

        Callable<Integer> receiveTickRequest = receiveTickRequest(time, ticks, latch);
        Callable<Integer> asyncProcessing = asyncProcessing();
        Callable<Integer> retrieveStatisticsRequest = receiveStatisticsRequest(ticks, latch);

        ExecutorService executor = Executors.newFixedThreadPool(6);
        executor.submit(receiveTickRequest);
        executor.submit(receiveTickRequest);
        executor.submit(retrieveStatisticsRequest);
        executor.submit(retrieveStatisticsRequest);
        executor.submit(retrieveStatisticsRequest);
        executor.submit(asyncProcessing);

        latch.await(3, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    private Callable<Integer> asyncProcessing() {
        return () -> {
            applicationService.doAsyncProcessing();
            return 0;
        };
    }

    private Callable<Integer> receiveTickRequest(long time, List<Tick> ticks, CountDownLatch latch) {
        return () -> {
            for (int i = 0; i < 100; i++) {
                var price = 100d + random.nextInt(100);
                var timestamp = time + random.nextInt(60_000);
                var tick = genTick(price, timestamp);

                applicationService.receiveTick(tick);
                ticks.add(tick);
                Thread.sleep(100);
            }
            latch.countDown();
            return 0;
        };
    }

    private Callable<Integer> receiveStatisticsRequest(List<Tick> ticks, CountDownLatch latch) {
        return () -> {
            try {
                var instrument = IndexApplicationService.ALL_INSTRUMENTS;
                for (int i = 0; i < 20; i++) {
                    if (applicationService.hasTicks()) {
                        var snapshot = applicationService.getStatSnapshots().get(instrument);
                        var source = new ArrayList<>(ticks);
                        assertStatistic(snapshot, source);
                    }
                    Thread.sleep(150);
                }
                latch.countDown();
            }catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        };
    }
}