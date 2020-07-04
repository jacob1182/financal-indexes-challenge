package com.example.financialindexes.domain;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class TickRepository {

    private final Queue<Tick> ticks = new ConcurrentLinkedQueue<>();

    /**
     * Persist a tick.
     * */
    public void save(Tick tick) {
        ticks.add(tick);
    }

    /**
     * Retrieve a tick by its timestamp.
     * */
    public Tick findByTimestamp(long timestamp) {
        return ticks.parallelStream()
                .filter(tick -> tick.getTimestamp() == timestamp)
                .findFirst()
                .orElse(null);
    }

    public List<Tick> findFreshTicks(long threshold) {
        return ticks.stream()
                .filter(tick -> tick.getTimestamp() >= threshold)
                .collect(Collectors.toList());
    }

    public void deleteAll() {
        ticks.clear();
    }
}
