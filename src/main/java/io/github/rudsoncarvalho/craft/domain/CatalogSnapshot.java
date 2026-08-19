package io.github.rudsoncarvalho.craft.domain;

/** Domain view of the catalog information required by the fulfillment use case. */
public record CatalogSnapshot(String orderId, String source, boolean available) {
    public static CatalogSnapshot degraded(String orderId) {
        return new CatalogSnapshot(orderId, "fallback-local", false);
    }
}
