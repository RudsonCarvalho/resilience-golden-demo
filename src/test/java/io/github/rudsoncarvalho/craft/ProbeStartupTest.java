package io.github.rudsoncarvalho.craft;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

/** Proves that the application starts an HTTP server and exposes real startup and liveness groups. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProbeStartupTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Test
    void applicationStartsAndLivenessAndStartupProbesAreUp() {
        String base = "http://localhost:" + port;
        String liveness = rest.getForObject(base + "/actuator/health/liveness", String.class);
        String startup = rest.getForObject(base + "/actuator/health/startup", String.class);

        assertThat(liveness).contains("\"status\":\"UP\"");
        assertThat(startup).contains("\"status\":\"UP\"");
    }
}
