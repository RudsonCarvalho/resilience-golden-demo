package io.github.rudsoncarvalho.reof.infra.messaging.kafka;

import io.github.rudsoncarvalho.reof.domain.FulfillmentEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;

/**
 * Encodes fulfillment events against an explicit Avro schema before Kafka publication.
 *
 * <p>The concrete schema makes REOF producer schema-validation evidence directly auditable.</p>
 */
@Component
public class OrderEventAvroEncoder {

    private static final Schema SCHEMA = new Schema.Parser().parse("""
            {
              "type": "record",
              "name": "FulfillmentEvent",
              "namespace": "io.github.rudsoncarvalho.reof.events",
              "fields": [
                {"name": "orderId", "type": "string"},
                {"name": "status", "type": "string"},
                {"name": "occurredAt", "type": "string"}
              ]
            }
            """);

    public byte[] encode(FulfillmentEvent event) {
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId is required by the event schema");
        }
        if (event.status() == null || event.status().isBlank()) {
            throw new IllegalArgumentException("status is required by the event schema");
        }
        if (event.occurredAt() == null) {
            throw new IllegalArgumentException("occurredAt is required by the event schema");
        }

        GenericRecord record = new GenericData.Record(SCHEMA);
        record.put("orderId", event.orderId());
        record.put("status", event.status());
        record.put("occurredAt", event.occurredAt().toString());

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            GenericDatumWriter<GenericRecord> writer = new GenericDatumWriter<>(SCHEMA);
            Encoder encoder = EncoderFactory.get().binaryEncoder(output, null);
            writer.write(record, encoder);
            encoder.flush();
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot encode Avro event", e);
        }
    }
}
