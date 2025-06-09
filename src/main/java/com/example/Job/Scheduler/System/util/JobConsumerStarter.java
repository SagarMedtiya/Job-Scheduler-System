package com.example.Job.Scheduler.System.util;

import com.example.Job.Scheduler.System.service.JobExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobConsumerStarter implements ApplicationListener<ApplicationStartedEvent> {
    private final JobExecutor jobExecutor;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        jobExecutor.startJobConsumer();
    }
}
