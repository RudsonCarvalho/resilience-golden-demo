package io.github.rudsoncarvalho.reof.infra.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Specific liveness check for a critical in-process runtime resource.
 *
 * <p>The probe can fail when the outbound executor is terminated, avoiding an unconditional static-200
 * liveness anti-pattern.</p>
 */
@Component("coreLiveness")
public class CoreLivenessHealthIndicator implements HealthIndicator {

    private final ThreadPoolTaskExecutor outboundExecutor;

    public CoreLivenessHealthIndicator(
            @Qualifier("outboundExecutor") ThreadPoolTaskExecutor outboundExecutor) {
        this.outboundExecutor = outboundExecutor;
    }

    @Override
    public Health health() {
        var pool = outboundExecutor.getThreadPoolExecutor();
        if (pool.isShutdown() || pool.isTerminated()) {
            return Health.down()
                    .withDetail("outboundExecutor", "terminated")
                    .build();
        }
        return Health.up()
                .withDetail("outboundExecutor", "running")
                .withDetail("activeThreads", pool.getActiveCount())
                .build();
    }
}
