package io.github.rudsoncarvalho.craft.application.port.out;

import io.github.rudsoncarvalho.craft.domain.OrderState;

/** Outbound application port for reading and writing service-owned order state. */
public interface OrderStatePort {
    OrderState loadOrCreate(String orderId);

    void store(OrderState state);
}
