package io.github.rudsoncarvalho.reof.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

final class GzipCodec {
    private GzipCodec() {}

    static byte[] compress(byte[] input) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
                gzip.write(input);
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to gzip webhook body", e);
        }
    }
}
