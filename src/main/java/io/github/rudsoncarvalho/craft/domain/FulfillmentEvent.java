package io.github.rudsoncarvalho.craft.domain;

import java.time.Instant;

/** Domain event emitted when an order enters the accepted fulfillment state. */
public record FulfillmentEvent(String orderId, String status, Instant occurredAt) {
    public static FulfillmentEvent accepted(String orderId) {
        return new FulfillmentEvent(orderId, "ACCEPTED", Instant.now());
    }
}
