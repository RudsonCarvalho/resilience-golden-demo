package io.github.rudsoncarvalho.craft.infra.client.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.junit.jupiter.api.Test;

/** Verifies that the webhook adapter produces a valid GZIP payload. */
class GzipCodecTest {

    @Test
    void compressesWebhookPayload() throws Exception {
        byte[] original = "craft-resilience-demo".getBytes(StandardCharsets.UTF_8);
        byte[] compressed = GzipCodec.compress(original);
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed))) {
            assertThat(gzip.readAllBytes()).isEqualTo(original);
        }
    }
}
