package io.github.rudsoncarvalho.craft.infra.health;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Startup gate that becomes healthy only after Spring publishes {@link ApplicationReadyEvent}. */
@Component("startupGate")
public class StartupHealthIndicator implements HealthIndicator {

    private final AtomicBoolean ready = new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    void onApplicationReady() {
        ready.set(true);
    }

    @Override
    public Health health() {
        return ready.get()
                ? Health.up().withDetail("applicationReadyEvent", true).build()
                : Health.outOfService().withDetail("applicationReadyEvent", false).build();
    }
}
