package io.github.rudsoncarvalho.craft.domain;

/** Domain-level acknowledgement for outbound fulfillment webhook delivery. */
public record WebhookReceipt(String orderId, String status) {
    public static WebhookReceipt degraded(String orderId) {
        return new WebhookReceipt(orderId, "FALLBACK_ACCEPTED");
    }
}
