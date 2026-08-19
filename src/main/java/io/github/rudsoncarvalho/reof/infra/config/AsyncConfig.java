package io.github.rudsoncarvalho.reof.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Infrastructure configuration for bounded asynchronous outbound work.
 *
 * <p>The explicit queue and thread limits prevent unbounded resource growth while webhook delivery is delayed.</p>
 */
@Configuration
public class AsyncConfig {

    @Bean("outboundExecutor")
    ThreadPoolTaskExecutor outboundExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(16);
        executor.setThreadNamePrefix("reof-outbound-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        executor.initialize();
        return executor;
    }
}
