package com.example.Job.Scheduler.System.util;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobRepository;
import com.example.Job.Scheduler.System.service.DistributedLockService;
import com.example.Job.Scheduler.System.service.JobExecutionService;
import com.example.Job.Scheduler.System.service.JobExecutor;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.xml.datatype.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecurringJobScheduler {

    private final JobExecutor jobExecutor;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final Map<UUID, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();
    private final JobExecutionService jobExecutionService;

    private final DistributedLockService distributedLockService;
    public void scheduleRecurringJob(Job job) {
        if (!job.isRecurring()) return;

        //Cancel existing schedule if presen
        cancelRecurringJob(job.getJobId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecution = job.getJobInterval().calculateNextExecution(now);
        ScheduledFuture<?> future = taskScheduler.schedule(
                () -> {

                    try {
                        // Verify it's time to execute (handle clock drift)
                        if (LocalDateTime.now().isBefore(nextExecution)) {
                            return;
                        }
                        jobExecutor.executeJob(job.getJobId());
                    }catch (Exception e) {
                        log.error("Error executing recurring job {}", job.getJobId(), e);
                    }
                },

                new CronTrigger(job.getCronExpression())
        );
        scheduledJobs.put(job.getJobId(), future);
        log.info("Scheduled recurring job {} with cronL {}", job.getJobId(), job.getCronExpression());
    }

    public void cancelRecurringJob(UUID jobId) {
        if(jobId == null){
            log.warn("Attempted to cancel a recurring job with null jobId.");
            return;
        }
        ScheduledFuture<?> future = scheduledJobs.get(jobId);
        if (future != null) {
            future.cancel(false);
            scheduledJobs.remove(jobId);
        }else{
            log.debug("No scheduled future found for jobId: {}", jobId);
        }
    }

    @PreDestroy
    public void cleanup() {
        scheduledJobs.values().forEach(f -> f.cancel(true));
        scheduledJobs.clear();
    }
}
