package io.github.rudsoncarvalho.reof.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.rudsoncarvalho.reof.domain.CatalogSnapshot;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CatalogClient {

    private final RestClient client;

    public CatalogClient(@Qualifier("catalogRestClient") RestClient client) {
        this.client = client;
    }

    // REOF CE: GET is naturally idempotent; Retry is outermost and falls back only after attempts are exhausted.
    @Retry(name = "catalog", fallbackMethod = "fallback")
    @CircuitBreaker(name = "catalog")
    public CatalogSnapshot consult(String orderId) {
        return client.get()
                .uri("/catalog/{orderId}", orderId)
                .retrieve()
                .body(CatalogSnapshot.class);
    }

    private CatalogSnapshot fallback(String orderId, Throwable failure) {
        // Deliberately does NOT call catalog again or any shared backend: avoids circular fallback.
        return CatalogSnapshot.degraded(orderId);
    }
}
