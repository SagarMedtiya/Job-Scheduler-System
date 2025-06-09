package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobRepository;
import com.example.Job.Scheduler.System.util.JobFailureHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobExecutor {

    private final JobRepository jobRepository;
    @Lazy
    private final JobExecutionService jobExecutionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String JOB_QUEUE_KEY = "jobs:queue";
    private final JobFailureHandler jobFailureHandler;


    public void enqueueJob(UUID jobId) {
        redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, jobId.toString());
    }
    @Async("taskExecutor")
    @Transactional
    public void executeJob(UUID jobId) {
        //Push Job to redis queue instead of executing directly
        log.info("Pushing job to redis queue: {}", jobId);
        Job job = jobRepository.findById(jobId).orElse(null);
        if(job == null || job.getStatus() != Job.JobStatus.PENDING){
            return;
        }

        // Mark as in queue
        job.setStatus(Job.JobStatus.IN_QUEUE);
        jobRepository.save(job);

        // Only enqueue if not already in queue
        List<Object> queue = redisTemplate.opsForList().range(JOB_QUEUE_KEY, 0, -1);
        if (!queue.contains(jobId.toString())) {
            redisTemplate.opsForList().leftPush(JOB_QUEUE_KEY, jobId.toString());
        }
    }
    @Async("taskExecutor")
    public void startJobConsumer() {
        while(!Thread.currentThread().isInterrupted()){
            String jobIdStr = null;
            try{
                //Blocking pop from Redis queue
                 jobIdStr =(String) redisTemplate.opsForList().rightPop(JOB_QUEUE_KEY, 10, TimeUnit.SECONDS);
                if(jobIdStr != null){
                    UUID jobId = UUID.fromString(jobIdStr);
                    processJobFromQueue(jobId);
                }
            }catch(Exception e){
                handleConsumerError(e, jobIdStr);
            }
        }
    }

    @Transactional
    public void processJobFromQueue(UUID jobId){
        Job job = jobRepository.findById(jobId).orElse(null);
        if(job == null){
            log.warn("Job {} not found in database", jobId);
            return;
        }
        //skip if not in PENDING state (already running or completed
        if(job.getStatus() != Job.JobStatus.PENDING && job.getStatus() != Job.JobStatus.IN_QUEUE){
            log.debug("Skipping job {} in status {}", jobId, job.getStatus());
            return;
        }
        jobExecutionService.executeJobWithLog(jobId);
    }

    private void handleConsumerError(Exception e, String jobIdStr) {
        log.error("Error processing job from Redis queue", e);

        if (jobIdStr != null) {
            try {
                UUID jobId = UUID.fromString(jobIdStr);
                Job job = jobRepository.findById(jobId).orElse(null);
                if (job != null) {
                    jobExecutionService.createExecutionLog(job, false, "Queue processing failed: " + e.getMessage());
                    jobFailureHandler.handleFailure(job, e.getMessage());
                }
            } catch (Exception ex) {
                log.error("Failed to handle failed job {}", jobIdStr, ex);
            }
        }

        try {
            Thread.sleep(1000); // Backoff period
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
    public void rescheduleRecurringJob(Job job){
        if(!job.isRecurring()){
            return;
        }
        job.setSchedule(
                job.getJobInterval().calculateNextExecution(job.getSchedule())
        );
        job.setStatus(Job.JobStatus.PENDING); // Ready for next run
        jobRepository.save(job);
        log.info("Rescheduled recurring job: {}", job.getJobId());
    }
}