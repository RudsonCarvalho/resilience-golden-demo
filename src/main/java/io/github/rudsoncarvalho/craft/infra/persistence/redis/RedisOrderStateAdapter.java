package io.github.rudsoncarvalho.craft.infra.persistence.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rudsoncarvalho.craft.application.port.out.OrderStatePort;
import io.github.rudsoncarvalho.craft.domain.OrderState;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Redis adapter for service-owned order state and the CRAFT internal-data boundary.
 *
 * <p>Redis is primary, while process-local state provides a fallback that never calls the failing backend again.</p>
 */
@Repository
public class RedisOrderStateAdapter implements OrderStatePort {

    private static final String PREFIX = "craft:order:";

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final ConcurrentMap<String, OrderState> localFallback = new ConcurrentHashMap<>();

    public RedisOrderStateAdapter(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
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

    @Override
    public void store(OrderState state) {
        localFallback.put(state.orderId(), state);
        try {
            redis.opsForValue().set(PREFIX + state.orderId(), serialize(state));
        } catch (RuntimeException redisFailure) {
            // Local state already preserves the write so the use case can continue without Redis.
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
