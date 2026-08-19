package io.github.rudsoncarvalho.craft.infra.client.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.rudsoncarvalho.craft.application.port.out.FulfillmentWebhookPort;
import io.github.rudsoncarvalho.craft.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.craft.domain.WebhookReceipt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP adapter responsible for sending fulfillment notifications to an external webhook.
 *
 * <p>This is the CRAFT external-exit boundary. The adapter combines idempotent PUT semantics, an idempotency
 * key, gzip, bounded async dispatch, timeout, pooling, circuit breaking, retry and local fallback.</p>
 */
@Component
public class FulfillmentWebhookHttpClient implements FulfillmentWebhookPort {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final Executor outboundExecutor;

    public FulfillmentWebhookHttpClient(
            @Qualifier("webhookRestClient") RestClient client,
            ObjectMapper objectMapper,
            @Qualifier("outboundExecutor") Executor outboundExecutor) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.outboundExecutor = outboundExecutor;
    }

    @Override
    @Retry(name = "webhook", fallbackMethod = "fallback")
    @CircuitBreaker(name = "webhook")
    public CompletableFuture<WebhookReceipt> dispatch(String orderId, FulfillmentEvent event) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] payload = GzipCodec.compress(serialize(event));
            client.put()
                    .uri("/fulfillments/{orderId}", orderId)
                    .header("Idempotency-Key", orderId)
                    .header("Content-Encoding", "gzip")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            return new WebhookReceipt(orderId, "DELIVERED");
        }, outboundExecutor);
    }

    private CompletableFuture<WebhookReceipt> fallback(String orderId, FulfillmentEvent event, Throwable failure) {
        return CompletableFuture.completedFuture(WebhookReceipt.degraded(orderId));
    }

    private byte[] serialize(FulfillmentEvent event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid fulfillment event", e);
        }
    }
}
