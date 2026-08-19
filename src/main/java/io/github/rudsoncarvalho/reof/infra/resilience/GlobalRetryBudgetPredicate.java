package io.github.rudsoncarvalho.reof.infra.resilience;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * Shared token-bucket predicate used by all outbound Resilience4j Retry instances.
 *
 * <p>A failed initial call may retry only when a global token is available. Sharing one budget across CE and SE
 * prevents independent downstream policies from multiplying retry traffic during a broad dependency incident.</p>
 */
public final class GlobalRetryBudgetPredicate implements Predicate<Throwable> {

    static final int CAPACITY = 20;
    static final int REFILL_TOKENS = 10;
    static final long REFILL_EVERY_NANOS = Duration.ofSeconds(1).toNanos();

    private static final AtomicInteger TOKENS = new AtomicInteger(CAPACITY);
    private static final AtomicLong LAST_REFILL = new AtomicLong(System.nanoTime());

    @Override
    public boolean test(Throwable failure) {
        refillIfNeeded();
        while (true) {
            int current = TOKENS.get();
            if (current <= 0) {
                return false;
            }
            if (TOKENS.compareAndSet(current, current - 1)) {
                return true;
            }
        }
    }

    private static void refillIfNeeded() {
        long now = System.nanoTime();
        long previous = LAST_REFILL.get();
        long elapsed = now - previous;
        if (elapsed < REFILL_EVERY_NANOS) {
            return;
        }

        long periods = elapsed / REFILL_EVERY_NANOS;
        long nextRefillMark = previous + periods * REFILL_EVERY_NANOS;
        if (!LAST_REFILL.compareAndSet(previous, nextRefillMark)) {
            return;
        }

        int add = Math.toIntExact(Math.min((long) CAPACITY, periods * REFILL_TOKENS));
        TOKENS.updateAndGet(current -> Math.min(CAPACITY, current + add));
    }
}
