package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryJobEnqueuer {

    private final JobRepository jobRepository;
    private final JobExecutor jobExecutor;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String JOB_QUEUE_KEY = "jobs:queue";
    private final Clock clock;

    @Scheduled(fixedRate = 60_00)
    public void enqueueDueRetryableJobs(){
        LocalDateTime now = LocalDateTime.now(clock);
        List<Job> dueJobs = jobRepository.findByRecurringAndStatusAndScheduleLessThanEqual(true,Job.JobStatus.PENDING, now);

        if (!dueJobs.isEmpty()) {
            log.info("Found {} jobs needing retry", dueJobs.size());
        }
        for (Job job : dueJobs) {
            jobExecutor.enqueueJob(job.getJobId());

            // Calculate and set next execution time
            LocalDateTime nextExecution = job.getJobInterval()
                    .calculateNextExecution(job.getSchedule());
            job.setSchedule(nextExecution);
            jobRepository.save(job);
        }
    }

}
























