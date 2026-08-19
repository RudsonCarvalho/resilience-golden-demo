package io.github.rudsoncarvalho.reof.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rudsoncarvalho.reof.domain.OrderState;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResilientOrderStateStore {

    private static final String PREFIX = "reof:order:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, OrderState> localFallback = new ConcurrentHashMap<>();

    public ResilientOrderStateStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    // REOF DI fallback: Redis is primary; bounded-process local state preserves reads/writes when Redis is unavailable.
    public OrderState loadOrCreate(String orderId) {
        try {
            String raw = redis.opsForValue().get(PREFIX + orderId);
            if (raw == null) {
                OrderState created = OrderState.initial(orderId);
                store(created);
                return created;
            }
            OrderState state = deserialize(raw).fromRedis();
            localFallback.put(orderId, state);
            return state;
        } catch (RuntimeException redisFailure) {
            return Optional.ofNullable(localFallback.get(orderId))
                    .map(OrderState::fromLocalFallback)
                    .orElseGet(() -> OrderState.initial(orderId).fromLocalFallback());
        }
    }

    public void store(OrderState state) {
        localFallback.put(state.orderId(), state);
        try {
            redis.opsForValue().set(PREFIX + state.orderId(), serialize(state));
        } catch (RuntimeException redisFailure) {
            // Intentional fallback already persisted the state in process memory.
        }
    }

    private String serialize(OrderState state) {
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize order state", e);
        }
    }

    private OrderState deserialize(String raw) {
        try {
            return objectMapper.readValue(raw, OrderState.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot deserialize order state", e);
        }
    }
}
