package io.github.rudsoncarvalho.reof.domain;

public record FulfillmentResponse(
        String orderId,
        String catalogSource,
        String stateSource,
        String webhookStatus,
        String kafkaStatus) {
}
