package com.example.financialindexes.app;

import com.example.financialindexes.domain.Tick;
import org.springframework.stereotype.Service;

@Service
public class TickApplicationService {

    public boolean receiveTick(Tick tick) {
        return tick.isFresh();
    }
}
