package io.github.rudsoncarvalho.reof.domain;

/**
 * Domain view of the catalog information required by the fulfillment use case.
 *
 * <p>The domain model does not know whether this data came from HTTP, a fallback, or any specific framework.</p>
 */
public record CatalogSnapshot(String orderId, String source, boolean available) {

    /**
     * Creates a safe degraded value when the catalog dependency cannot be reached.
     */
    public static CatalogSnapshot degraded(String orderId) {
        return new CatalogSnapshot(orderId, "fallback-local", false);
    }
}
