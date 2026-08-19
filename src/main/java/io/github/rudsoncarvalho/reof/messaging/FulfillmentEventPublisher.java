package io.github.rudsoncarvalho.reof.messaging;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class FulfillmentEventPublisher {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final OrderEventAvroEncoder encoder;
    private final KafkaFailureBuffer failureBuffer;
    private final String topic;

    public FulfillmentEventPublisher(
            KafkaTemplate<String, byte[]> kafkaTemplate,
            OrderEventAvroEncoder encoder,
            KafkaFailureBuffer failureBuffer,
            @Value("${demo.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.encoder = encoder;
        this.failureBuffer = failureBuffer;
        this.topic = topic;
    }

    // REOF SE-KAFKA throttling: explicit producer rate limiter, separate from HTTP entry protection.
    @RateLimiter(name = "kafkaProducerThrottle")
    public CompletableFuture<SendResult<String, byte[]>> publish(FulfillmentEvent event) {
        byte[] schemaValidatedPayload = encoder.encode(event);
        CompletableFuture<SendResult<String, byte[]>> send =
                kafkaTemplate.send(topic, event.orderId(), schemaValidatedPayload);
        send.whenComplete((result, error) -> {
            if (error != null) {
                failureBuffer.record(event.orderId(), error); // explicit failure path
            }
        });
        return send;
    }
}
