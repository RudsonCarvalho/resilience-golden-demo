package io.github.rudsoncarvalho.reof.application.port.out;

import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import java.util.concurrent.CompletableFuture;

/**
 * Outbound application port for publishing fulfillment events.
 *
 * <p>The contract deliberately avoids Kafka-specific types so infrastructure concerns do not leak into the
 * application layer.</p>
 */
public interface FulfillmentEventPort {
    CompletableFuture<Void> publish(FulfillmentEvent event);
}
