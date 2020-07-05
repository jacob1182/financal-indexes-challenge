package com.example.financialindexes.app;

import com.example.financialindexes.domain.Statistics;
import com.example.financialindexes.domain.StatisticsSnapshot;
import com.example.financialindexes.domain.Tick;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IndexApplicationService {

    private StatisticsSnapshot statSnapshot = StatisticsSnapshot.getNewInstance();

    public boolean receiveTick(Tick tick) {
        if (!tick.isFresh(System.currentTimeMillis()))
            return false;

        statSnapshot = statSnapshot.withTick(tick);

        return true;
    }

    public Statistics getStatistics() {
        return statSnapshot.getStatistics();
    }

    public void clearTicks() {
        statSnapshot = StatisticsSnapshot.getNewInstance();
    }
}
