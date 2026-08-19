package io.github.rudsoncarvalho.reof.infra.messaging.kafka;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.rudsoncarvalho.reof.application.port.out.FulfillmentEventPort;
import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

/**
 * Kafka adapter for fulfillment-event publication.
 *
 * <p>This is the REOF SE-KAFKA boundary. Schema validation, producer throttling, idempotent producer settings,
 * bounded batching, {@code acks=all}, and an explicit bounded failure path are all visible in repository code
 * or configuration.</p>
 */
@Component
public class KafkaFulfillmentEventPublisher implements FulfillmentEventPort {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final OrderEventAvroEncoder encoder;
    private final KafkaFailureBuffer failureBuffer;
    private final String topic;

    public KafkaFulfillmentEventPublisher(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            OrderEventAvroEncoder encoder,
            KafkaFailureBuffer failureBuffer,
            @Value("${demo.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.encoder = encoder;
        this.failureBuffer = failureBuffer;
        this.topic = topic;
    }

    @Override
    @RateLimiter(name = "kafkaProducerThrottle")
    public CompletableFuture<Void> publish(FulfillmentEvent event) {
        byte[] schemaValidatedPayload = encoder.encode(event);
        try {
            CompletableFuture<SendResult<String, byte[]>> send =
                    kafkaTemplate.send(topic, event.orderId(), schemaValidatedPayload);
            send.whenComplete((result, error) -> {
                if (error != null) {
                    failureBuffer.record(event.orderId(), error);
                }
            });
            return send.thenApply(ignored -> null);
        } catch (RuntimeException failure) {
            failureBuffer.record(event.orderId(), failure);
            return CompletableFuture.failedFuture(failure);
        }
    }
}
