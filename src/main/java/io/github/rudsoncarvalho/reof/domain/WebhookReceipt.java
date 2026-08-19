package io.github.rudsoncarvalho.reof.domain;

public record WebhookReceipt(String orderId, String status) {
    public static WebhookReceipt degraded(String orderId) {
        return new WebhookReceipt(orderId, "FALLBACK_ACCEPTED");
    }
}
