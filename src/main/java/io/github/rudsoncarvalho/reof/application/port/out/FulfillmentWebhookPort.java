package io.github.rudsoncarvalho.reof.application.port.out;

import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.reof.domain.WebhookReceipt;
import java.util.concurrent.CompletableFuture;

/**
 * Outbound application port for asynchronous webhook delivery.
 */
public interface FulfillmentWebhookPort {
    CompletableFuture<WebhookReceipt> dispatch(String orderId, FulfillmentEvent event);
}
