package io.github.rudsoncarvalho.craft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the resilience golden-demo application used as a CRAFT positive-control fixture.
 *
 * <p>The root package contains only the application bootstrap. Business concepts live in {@code domain},
 * use-case orchestration and ports live in {@code application}, and technology-specific adapters live in
 * {@code infra}.</p>
 */
@SpringBootApplication
public class ResilienceGoldenDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResilienceGoldenDemoApplication.class, args);
    }
}
