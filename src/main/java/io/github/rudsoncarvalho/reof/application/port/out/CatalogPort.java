package io.github.rudsoncarvalho.reof.application.port.out;

import io.github.rudsoncarvalho.reof.domain.CatalogSnapshot;

/**
 * Outbound application port for the synchronous catalog consultation.
 *
 * <p>The application layer depends on this contract instead of an HTTP client implementation.</p>
 */
public interface CatalogPort {
    CatalogSnapshot consult(String orderId);
}
