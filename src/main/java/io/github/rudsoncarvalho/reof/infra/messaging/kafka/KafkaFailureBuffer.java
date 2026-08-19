package io.github.rudsoncarvalho.reof.infra.messaging.kafka;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.stereotype.Component;

/**
 * Bounded local failure path for Kafka publication errors.
 *
 * <p>The buffer intentionally does not republish to the same failing Kafka backend, which avoids a circular
 * failure path while keeping failed-send evidence available inside the process.</p>
 */
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
