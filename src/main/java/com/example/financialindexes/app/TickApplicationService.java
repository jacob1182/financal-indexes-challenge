package com.example.financialindexes.app;

import com.example.financialindexes.domain.Statistics;
import com.example.financialindexes.domain.Tick;
import com.example.financialindexes.domain.TickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TickApplicationService {

    private final TickRepository ticks;
    private Statistics currentStats = Statistics.EMPTY;

    public boolean receiveTick(Tick tick) {
        if (!tick.isFresh())
            return false;

        ticks.save(tick);

        currentStats = Statistics.calculate(ticks.findAll(), System.currentTimeMillis() - 60_000);

        return true;
    }

    public Statistics getStatistics() {
        return currentStats;
    }
}
