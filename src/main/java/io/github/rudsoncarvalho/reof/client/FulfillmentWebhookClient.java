package io.github.rudsoncarvalho.reof.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import io.github.rudsoncarvalho.reof.domain.WebhookReceipt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FulfillmentWebhookClient {

    private final RestClient client;
    private final ObjectMapper objectMapper;
    private final Executor outboundExecutor;

    public FulfillmentWebhookClient(
            @Qualifier("webhookRestClient") RestClient client,
            ObjectMapper objectMapper,
            @Qualifier("outboundExecutor") Executor outboundExecutor) {
        this.client = client;
        this.objectMapper = objectMapper;
        this.outboundExecutor = outboundExecutor;
    }

    // REOF SE: asynchronous dispatch + gzip + pooled client timeout + CB + bounded retry.
    // PUT is naturally idempotent and the explicit Idempotency-Key makes retry safety auditable.
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

    private CompletableFuture<WebhookReceipt> fallback(
            String orderId, FulfillmentEvent event, Throwable failure) {
        // Deliberately local: same target/backend is never called by the fallback.
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
