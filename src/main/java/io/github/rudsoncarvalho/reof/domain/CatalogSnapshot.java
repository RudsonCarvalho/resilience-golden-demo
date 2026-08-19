package io.github.rudsoncarvalho.reof.domain;

public record CatalogSnapshot(String orderId, String source, boolean available) {
    public static CatalogSnapshot degraded(String orderId) {
        return new CatalogSnapshot(orderId, "fallback-local", false);
    }
}
