package com.example.Job.Scheduler.System.util;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobRepository;
import com.example.Job.Scheduler.System.service.JobRetryLaterService;
import com.example.Job.Scheduler.System.service.NotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.LocalDateTime;
import java.util.List;


@Component
@Slf4j
public class JobFailureHandler {
    private final JobRepository jobRepository;
    private final NotificationService notificationService;
    private final JobRetryLaterService retryLaterService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MeterRegistry meterRegistry;
    private static final String JOB_QUEUE_KEY = "jobs:queue"; // Use same key as consumer
    private static final String DLQ_KEY = "jobs:dead-letter-queue"; // Dead Letter Queue

    public JobFailureHandler(JobRepository jobRepository, NotificationService notificationService, JobRetryLaterService jobRetryLaterService, RedisTemplate<String, Object> redisTemplate, MeterRegistry meterRegistry) {
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
        this.retryLaterService = jobRetryLaterService;
        this.redisTemplate = redisTemplate;
        this.meterRegistry = meterRegistry;
    }

    public void handleFailure(Job job, String failureMessage) {

        job.setFailureMessage(failureMessage);
        job.setRetries(job.getRetries() + 1);

        if (job.getRetries() >= job.getMaxRetries()) {
            job.setStatus(Job.JobStatus.FAILED);
            // Push to DLQ
            redisTemplate.opsForList().leftPush(DLQ_KEY, job.getJobId().toString());
            meterRegistry.counter("job.queue.ops", "type", "dlq").increment();

            log.warn("Job {} exceeded max retries. Moved to DLQ", job.getJobId());
            notificationService.notifyUser(job.getJobId(), "Job failed after " + job.getMaxRetries() + " retries: " + failureMessage);
        } else {
            job.setStatus(Job.JobStatus.PENDING);
            job.setSchedule(LocalDateTime.now().plusMinutes(1)); // retry after 1 minute
            // Push back to main queue for retry
            List<Object> existingJobs = redisTemplate.opsForList().range(JOB_QUEUE_KEY,0, -1);
            if(!existingJobs.contains(job.getJobId().toString())){
                redisTemplate.opsForList().leftPush(JOB_QUEUE_KEY, job.getJobId().toString());
                log.info("Job {} re-enqueued for immediate retry", job.getJobId());
            }
            else{
                log.info("Job {} already in queue, skipping duplicate enqueue", job.getJobId());
            }
            meterRegistry.counter("job.queue.ops", "type", "retry").increment();

            log.info("Retrying job {} (attempt {}/{})",
                    job.getJobName(),
                    job.getRetries(),
                    job.getMaxRetries()
            );
        }

        jobRepository.save(job);
    }
}
