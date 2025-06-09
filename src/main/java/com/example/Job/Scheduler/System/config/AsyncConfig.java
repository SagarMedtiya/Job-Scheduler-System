package com.example.Job.Scheduler.System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.time.Clock;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name= "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(5); //min threads
        executor.setMaxPoolSize(10); //max threads
        executor.setQueueCapacity(25); //Queue size for task
        executor.setThreadNamePrefix("Job-Consumer-");
        executor.initialize();

        return executor;
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
