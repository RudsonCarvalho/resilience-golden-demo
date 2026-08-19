package io.github.rudsoncarvalho.reof;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Boots the REOF golden demo application.
 *
 * <p>The root package intentionally contains only the application bootstrap. Business concepts live in
 * {@code domain}, use-case orchestration and ports live in {@code application}, and technology-specific
 * adapters live in {@code infra}.</p>
 */
@SpringBootApplication
public class ReofGoldenDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReofGoldenDemoApplication.class, args);
    }
}
