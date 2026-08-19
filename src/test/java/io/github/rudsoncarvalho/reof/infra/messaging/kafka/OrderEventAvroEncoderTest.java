package io.github.rudsoncarvalho.reof.infra.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Verifies that Kafka events are encoded against the declared Avro schema and invalid events are rejected.
 */
class OrderEventAvroEncoderTest {

    private final OrderEventAvroEncoder encoder = new OrderEventAvroEncoder();

    @Test
    void encodesValidEventAgainstSchema() {
        byte[] encoded = encoder.encode(new FulfillmentEvent("order-1", "ACCEPTED", Instant.EPOCH));
        assertThat(encoded).isNotEmpty();
    }

    @Test
    void rejectsMissingRequiredSchemaField() {
        assertThatThrownBy(() -> encoder.encode(new FulfillmentEvent("", "ACCEPTED", Instant.EPOCH)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
