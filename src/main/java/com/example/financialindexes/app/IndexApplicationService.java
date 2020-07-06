package com.example.financialindexes.app;

import com.example.financialindexes.domain.Statistics;
import com.example.financialindexes.domain.StatisticsSnapshot;
import com.example.financialindexes.domain.Tick;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class IndexApplicationService {

    public static final String ALL_INSTRUMENTS = "ALL_INSTRUMENTS";

    @Getter
    private final Map<String, StatisticsSnapshot> statSnapshots = new HashMap<>();

    private final Semaphore semaphore = new Semaphore(1);

    public boolean receiveTick(Tick tick) {
        var time = System.currentTimeMillis();
        if (!tick.isFresh(time))
            return false;

        try {
            semaphore.acquire();
            registerTick(tick, time, tick.getInstrument());
            registerTick(tick, time, ALL_INSTRUMENTS);

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        } finally {
            semaphore.release();
        }

        return true;
    }

    private void registerTick(Tick tick, long time, String instrument) {
        var snapshot = statSnapshots.getOrDefault(instrument, StatisticsSnapshot.getNewInstance());
        snapshot = snapshot.withTick(time, tick);
        statSnapshots.put(instrument, snapshot);
    }

    @Scheduled(fixedRate = 200)
    public void asyncRecalculateStats() {
        try {
            if (!semaphore.tryAcquire())
                return;

            statSnapshots.keySet().forEach(instrument ->
                    statSnapshots.computeIfPresent(instrument,
                            (__, snapshot) -> snapshot.recalculate(System.currentTimeMillis())));

        } finally {
            semaphore.release();
        }
    }

    public Statistics getStatistics() {
        return getStatistics(ALL_INSTRUMENTS);
    }

    public Statistics getStatistics(String instrument) {
        return statSnapshots.getOrDefault(instrument, StatisticsSnapshot.getNewInstance()).getStatistics();
    }

    public void clearTicks() {
        statSnapshots.clear();
    }
}
