package io.github.rudsoncarvalho.reof.domain;

/**
 * Result returned by the fulfillment use case.
 *
 * <p>The source/status fields intentionally make healthy and degraded execution visible in the demo.</p>
 */
public record FulfillmentResponse(
        String orderId,
        String catalogSource,
        String stateSource,
        String webhookStatus,
        String kafkaStatus) {
}
