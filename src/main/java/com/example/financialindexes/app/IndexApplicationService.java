package com.example.financialindexes.app;

import com.example.financialindexes.domain.Statistics;
import com.example.financialindexes.domain.StatisticsSnapshot;
import com.example.financialindexes.domain.Tick;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class IndexApplicationService {

    public static final String ALL_INSTRUMENTS = "ALL_INSTRUMENTS";

    @Getter
    private final Map<String, StatisticsSnapshot> statSnapshots = new HashMap<>();

    private final BlockingDeque<Tick> receivedTicks = new LinkedBlockingDeque<>();

    public boolean receiveTick(Tick tick) {
        var time = System.currentTimeMillis();
        if (!tick.isFresh(time))
            return false;

        receivedTicks.offer(tick);

        return true;
    }

    @Async
    public void doAsyncProcessing() {
        try {
            while (true)
                processReceivedTicks();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    private void processReceivedTicks() throws InterruptedException {
        var tick = receivedTicks.poll(10, TimeUnit.MILLISECONDS);
        if (tick == null) {
            forceStatRecalculation();
        } else {
            var time = System.currentTimeMillis();
            registerTick(tick, time, ALL_INSTRUMENTS);
            registerTick(tick, time, tick.getInstrument());
        }
    }

    private void registerTick(Tick tick, long time, String instrument) {
        var snapshot = statSnapshots.getOrDefault(instrument, StatisticsSnapshot.getNewInstance());
        snapshot = snapshot.withTick(time, tick);
        statSnapshots.put(instrument, snapshot);
    }

    public void forceStatRecalculation() {
        statSnapshots.forEach((instrument, snapshot) ->
                statSnapshots.put(instrument, snapshot.recalculate(System.currentTimeMillis())));
    }

    public Statistics getStatistics() {
        return getStatistics(ALL_INSTRUMENTS);
    }

    public Statistics getStatistics(String instrument) {
        return statSnapshots.getOrDefault(instrument, StatisticsSnapshot.getNewInstance()).getStatistics();
    }

    public boolean hasTicks () {
        return getStatistics().getCount() > 0;
    }

    public void clearTicks() {
        statSnapshots.clear();
    }
}
