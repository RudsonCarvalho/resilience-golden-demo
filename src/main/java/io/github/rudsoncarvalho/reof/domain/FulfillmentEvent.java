package io.github.rudsoncarvalho.reof.domain;

import java.time.Instant;

/**
 * Domain event emitted when an order enters the accepted fulfillment state.
 */
public record FulfillmentEvent(String orderId, String status, Instant occurredAt) {

    /**
     * Creates the canonical event produced by the fulfillment use case.
     */
    public static FulfillmentEvent accepted(String orderId) {
        return new FulfillmentEvent(orderId, "ACCEPTED", Instant.now());
    }
}
