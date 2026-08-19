package io.github.rudsoncarvalho.craft.domain;

/** Response that makes healthy and degraded execution paths visible in the demo. */
public record FulfillmentResponse(
        String orderId,
        String catalogSource,
        String stateSource,
        String webhookStatus,
        String kafkaStatus) {
}
