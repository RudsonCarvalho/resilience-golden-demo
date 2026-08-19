package io.github.rudsoncarvalho.craft.application.port.out;

import io.github.rudsoncarvalho.craft.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.craft.domain.WebhookReceipt;
import java.util.concurrent.CompletableFuture;

/** Outbound application port for asynchronous webhook delivery. */
public interface FulfillmentWebhookPort {
    CompletableFuture<WebhookReceipt> dispatch(String orderId, FulfillmentEvent event);
}
