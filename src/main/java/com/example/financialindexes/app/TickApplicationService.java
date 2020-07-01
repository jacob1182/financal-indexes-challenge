package com.example.financialindexes.app;

import com.example.financialindexes.domain.Tick;
import com.example.financialindexes.domain.TickRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TickApplicationService {

    private final TickRepository ticks;

    public boolean receiveTick(Tick tick) {
        if (!tick.isFresh())
            return false;

        ticks.save(tick);

        return true;
    }
}
