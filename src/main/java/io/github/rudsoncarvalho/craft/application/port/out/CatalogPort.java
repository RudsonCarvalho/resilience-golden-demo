package io.github.rudsoncarvalho.craft.application.port.out;

import io.github.rudsoncarvalho.craft.domain.CatalogSnapshot;

/** Outbound application port for the synchronous catalog consultation. */
public interface CatalogPort {
    CatalogSnapshot consult(String orderId);
}
