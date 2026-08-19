package io.github.rudsoncarvalho.reof.infra.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Specific readiness check that verifies the Redis dependency with a real PING operation.
 */
@Component("redisReadiness")
public class RedisReadinessHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    public RedisReadinessHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String pong = connection.ping();
            return "PONG".equalsIgnoreCase(pong)
                    ? Health.up().withDetail("redisPing", pong).build()
                    : Health.down().withDetail("redisPing", pong).build();
        } catch (RuntimeException failure) {
            return Health.down(failure).build();
        }
    }
}
