package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.util.RecurringJobScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSchedulingService {
    @Lazy
    private final RecurringJobScheduler recurringJobScheduler;

    public void cancelIfRecurring(Job job) {
        if (job.isRecurring()) {
            UUID jobId = job.getJobId();
            if (jobId != null) {
                recurringJobScheduler.cancelRecurringJob(jobId);
            } else {
                log.warn("Tried to cancel recurring job with null jobId. Job: {}", job);
            }
        }
    }
    public void scheduleIfRecurring(Job job) {
        if (job.isRecurring()) {
            recurringJobScheduler.scheduleRecurringJob(job);
        }
    }
}
