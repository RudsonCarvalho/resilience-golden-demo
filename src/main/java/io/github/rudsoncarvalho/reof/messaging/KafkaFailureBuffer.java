package io.github.rudsoncarvalho.reof.messaging;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.stereotype.Component;

@Component
public class KafkaFailureBuffer {

    private static final int MAX_FAILURES = 100;
    private final Deque<FailureRecord> failures = new ArrayDeque<>(MAX_FAILURES);

    // Explicit, bounded failure path for producer errors; it never republishes to the failing Kafka backend.
    public synchronized void record(String key, Throwable error) {
        if (failures.size() == MAX_FAILURES) {
            failures.removeFirst();
        }
        failures.addLast(new FailureRecord(key, error.getClass().getSimpleName(), Instant.now()));
    }

    public synchronized int size() {
        return failures.size();
    }

    record FailureRecord(String key, String errorType, Instant occurredAt) {}
}
