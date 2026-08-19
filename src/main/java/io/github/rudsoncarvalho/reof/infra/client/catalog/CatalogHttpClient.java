package io.github.rudsoncarvalho.reof.infra.client.catalog;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.rudsoncarvalho.reof.application.port.out.CatalogPort;
import io.github.rudsoncarvalho.reof.domain.CatalogSnapshot;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP adapter that implements the catalog consultation port.
 *
 * <p>This is the REOF CE boundary. Timeout and pooling are configured by the shared HTTP infrastructure, while
 * circuit breaking, bounded retry and fallback are attached to this concrete interaction point.</p>
 */
@Component
public class CatalogHttpClient implements CatalogPort {

    private final RestClient client;

    public CatalogHttpClient(@Qualifier("catalogRestClient") RestClient client) {
        this.client = client;
    }

    /**
     * Consults the external catalog using an idempotent GET operation. Retry is intentionally bounded and the
     * fallback is local, so it never calls the failed backend again.
     */
    @Override
    @Retry(name = "catalog", fallbackMethod = "fallback")
    @CircuitBreaker(name = "catalog")
    public CatalogSnapshot consult(String orderId) {
        return client.get()
                .uri("/catalog/{orderId}", orderId)
                .retrieve()
                .body(CatalogSnapshot.class);
    }

    private CatalogSnapshot fallback(String orderId, Throwable failure) {
        return CatalogSnapshot.degraded(orderId);
    }
}
