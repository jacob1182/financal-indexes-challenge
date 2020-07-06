package com.example.financialindexes.app;

import com.example.financialindexes.domain.Statistics;
import com.example.financialindexes.domain.StatisticsSnapshot;
import com.example.financialindexes.domain.Tick;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.Semaphore;

@Service
@RequiredArgsConstructor
public class IndexApplicationService {

    @Getter
    private StatisticsSnapshot statSnapshot = StatisticsSnapshot.getNewInstance();

    private final Semaphore semaphore = new Semaphore(1);

    public boolean receiveTick(Tick tick) {
        var time = System.currentTimeMillis();
        if (!tick.isFresh(time))
            return false;

        try {
            semaphore.acquire();
            statSnapshot = statSnapshot.withTick(time, tick);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        } finally {
            semaphore.release();
        }

        return true;
    }

    @Scheduled(fixedRate = 500)
    public void asyncRecalculateStats() {
        try {
            if (!semaphore.tryAcquire())
                return;

            statSnapshot = statSnapshot.recalculate(System.currentTimeMillis());
        } finally {
            semaphore.release();
        }
    }

    public Statistics getStatistics() {
        return statSnapshot.getStatistics();
    }

    public void clearTicks() {
        statSnapshot = StatisticsSnapshot.getNewInstance();
    }
}
