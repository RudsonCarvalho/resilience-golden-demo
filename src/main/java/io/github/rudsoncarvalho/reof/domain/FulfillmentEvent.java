package io.github.rudsoncarvalho.reof.domain;

import java.time.Instant;

public record FulfillmentEvent(String orderId, String status, Instant occurredAt) {
    public static FulfillmentEvent accepted(String orderId) {
        return new FulfillmentEvent(orderId, "ACCEPTED", Instant.now());
    }
}
