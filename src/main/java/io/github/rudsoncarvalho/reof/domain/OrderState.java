package io.github.rudsoncarvalho.reof.domain;

public record OrderState(String orderId, String status, String source) {
    public static OrderState initial(String orderId) {
        return new OrderState(orderId, "NEW", "generated");
    }

    public OrderState fromRedis() {
        return new OrderState(orderId, status, "redis");
    }

    public OrderState fromLocalFallback() {
        return new OrderState(orderId, status, "local-fallback");
    }
}
