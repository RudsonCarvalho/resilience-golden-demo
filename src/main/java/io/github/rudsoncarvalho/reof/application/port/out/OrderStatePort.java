package io.github.rudsoncarvalho.reof.application.port.out;

import io.github.rudsoncarvalho.reof.domain.OrderState;

/**
 * Outbound application port for reading and writing the order state owned by this service.
 */
public interface OrderStatePort {
    OrderState loadOrCreate(String orderId);

    void store(OrderState state);
}
