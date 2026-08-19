package io.github.rudsoncarvalho.craft.infra.messaging.kafka;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.stereotype.Component;

/** Bounded local failure path for Kafka errors that never republishes to the same failing backend. */
@Component
public class KafkaFailureBuffer {

    private static final int MAX_FAILURES = 100;
    private final Deque<FailureRecord> failures = new ArrayDeque<>(MAX_FAILURES);

    public synchronized void record(String key, Throwable error) {
        if (failures.size() == MAX_FAILURES) {
            failures.removeFirst();
        }
        failures.addLast(new FailureRecord(key, error.getClass().getSimpleName(), Instant.now()));
    }

    public synchronized int size() {
        return failures.size();
    }

    record FailureRecord(String key, String errorType, Instant occurredAt) {
    }
}
