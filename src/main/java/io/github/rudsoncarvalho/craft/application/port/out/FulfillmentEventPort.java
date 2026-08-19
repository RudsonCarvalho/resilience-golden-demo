package io.github.rudsoncarvalho.craft.application.port.out;

import io.github.rudsoncarvalho.craft.domain.FulfillmentEvent;
import java.util.concurrent.CompletableFuture;

/** Outbound application port for publishing fulfillment events without leaking Kafka types. */
public interface FulfillmentEventPort {
    CompletableFuture<Void> publish(FulfillmentEvent event);
}
