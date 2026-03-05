package com.app.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration for Non-Blocking Operations
 * Optimized for logging and background tasks
 */
@Configuration
public class AsyncConfig {

    /**
     * Thread pool for async operations (like logging)
     * Configured with optimal settings to avoid resource exhaustion
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size - minimum threads always alive
        executor.setCorePoolSize(2);
        
        // Max pool size - maximum threads that can be created
        executor.setMaxPoolSize(5);
        
        // Queue capacity - tasks waiting for execution
        executor.setQueueCapacity(100);
        
        // Thread name prefix for easy identification in logs
        executor.setThreadNamePrefix("Async-Logging-");
        
        // Graceful shutdown - wait for tasks to complete
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
