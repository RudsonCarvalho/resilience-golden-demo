package io.github.rudsoncarvalho.craft.infra.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.rudsoncarvalho.craft.domain.FulfillmentEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Verifies Avro encoding and rejection of events missing required schema fields. */
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
