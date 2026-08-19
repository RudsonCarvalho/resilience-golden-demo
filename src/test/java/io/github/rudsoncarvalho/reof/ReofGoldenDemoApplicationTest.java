package io.github.rudsoncarvalho.reof;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Verifies that the Spring context resolves every named Resilience4j instance used by the golden fixture.
 */
@SpringBootTest
class ReofGoldenDemoApplicationTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakers;

    @Autowired
    private RetryRegistry retries;

    @Autowired
    private BulkheadRegistry bulkheads;

    @Autowired
    private RateLimiterRegistry rateLimiters;

    @Test
    void contextLoadsWithAllScoredResilienceInstancesResolvable() {
        assertThat(circuitBreakers.circuitBreaker("catalog").getCircuitBreakerConfig().getFailureRateThreshold())
                .isEqualTo(50.0f);
        assertThat(circuitBreakers.circuitBreaker("webhook").getCircuitBreakerConfig().getSlidingWindowSize())
                .isEqualTo(10);
        assertThat(retries.retry("catalog").getRetryConfig().getMaxAttempts()).isEqualTo(3);
        assertThat(retries.retry("webhook").getRetryConfig().getMaxAttempts()).isEqualTo(3);
        assertThat(bulkheads.bulkhead("entryBulkhead").getBulkheadConfig().getMaxConcurrentCalls())
                .isEqualTo(32);
        assertThat(rateLimiters.rateLimiter("kafkaProducerThrottle").getRateLimiterConfig().getLimitForPeriod())
                .isEqualTo(50);
    }
}
