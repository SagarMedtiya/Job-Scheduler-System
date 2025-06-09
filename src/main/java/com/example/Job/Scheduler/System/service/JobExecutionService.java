package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.entity.JobExecutionLog;
import com.example.Job.Scheduler.System.repository.JobExecutionLogRepository;
import com.example.Job.Scheduler.System.repository.JobRepository;
import com.example.Job.Scheduler.System.util.JobFailureHandler;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobExecutionService {
    private final JobRepository jobRepository;
    private final JobExecutionLogRepository logRepository;
    private final NotificationService notificationService;
    private final JobFailureHandler jobFailureHandler;
    private final MeterRegistry meterRegistry;
    private final DistributedLockService lockService;
    private final JobStatusTracker jobStatusTracker;
    private final JobRetryLaterService jobRetryLaterService;
    private final JobExecutionLogRepository jobExecutionLogRepository;


    @Transactional
    public void executeJobWithLog(UUID jobId){
        //redis implementation logic
        String lockKey = "job:exec" + jobId;
        String lockValue = UUID.randomUUID().toString();
        try{
            //Acquire distributesd lock with timeout
            if(!lockService.acquireLock(lockKey, lockValue)){
                log.warn("Could not acquire lock for job {}", jobId);
                return;
            }

            // Add optimistic locking version check
            Job job = jobRepository.findByIdWithLock(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            // Skip if job is already completed
            if (job.getStatus() == Job.JobStatus.COMPLETED) {
                log.debug("Skipping already completed job {}", jobId);
                return;
            }
            JobExecutionLog logEntry = new JobExecutionLog();
            logEntry.setJob(job);
            logEntry.setStartTime(LocalDateTime.now());
            logEntry.setStatus(Job.JobStatus.RUNNING);
            logRepository.save(logEntry);

            try{
                // Update job status to RUNNING
                job.setStatus(Job.JobStatus.RUNNING);
                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                jobStatusTracker.updateJobStatus(job.getJobId(), Job.JobStatus.RUNNING);
                log.info("Executing job {}", job.getJobName());

                // Simulate job execution
                Thread.sleep(1000);

                // Random failure simulation (30% chance) for testing purposes
                if (Math.random() < 0.3) {
                    throw new RuntimeException("Simulated job execution failure");
                }

                // Handle successful execution
                logEntry.setEndTime(LocalDateTime.now());
                logEntry.setStatus(Job.JobStatus.COMPLETED);
                logRepository.save(logEntry);

                // Reset retry counters on success
                job.setRetries(0);
                job.setNextRetryAt(null);

                // Update job status based on recurring flag
                if (job.isRecurring()) {
                    job.setStatus(Job.JobStatus.PENDING);
                    log.info("Recurring job {} completed, reset to PENDING for next run", job.getJobId());
                } else {
                    job.setStatus(Job.JobStatus.COMPLETED);
                }


                job.setUpdatedAt(LocalDateTime.now());
                jobRepository.save(job);

                // Notify and track
                notificationService.notifyUser(
                        job.getJobId(),
                        "Job completed successfully" + (job.isRecurring() ? " (recurring)" : "")
                );
                jobStatusTracker.updateJobStatus(job.getJobId(), job.isRecurring() ? Job.JobStatus.PENDING : Job.JobStatus.COMPLETED);
                meterRegistry.counter("job.executed", "status", "success").increment();
            }catch(Exception e){
                log.error("Job {} execution failed", job.getJobId(), e);

                // Update log entry
                logEntry.setEndTime(LocalDateTime.now());
                logEntry.setStatus(Job.JobStatus.FAILED);
                logEntry.setExecutionMessage(e.getMessage());
                logRepository.save(logEntry);

                //retry logic
                if(job.getRetries() < job.getMaxRetries()){
                    scheduleRetry(job,e);
                }
                else{
                    markAsFailed(job,e);
                }
            }
        }
        finally {
            lockService.releaseLock(lockKey, lockValue);
        }
    }
    private void scheduleRetry(Job job, Exception e){
        int newRetryCount = job.getRetries() + 1;

        job.setRetries(newRetryCount);
        job.setStatus(Job.JobStatus.PENDING);
        job.setNextRetryAt(null);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        jobRetryLaterService.retryLater(job.getJobId());

        log.info("Immediately retrying job {} (attempt {}/{})",
                job.getJobId(), job.getRetries(), job.getMaxRetries());

        notificationService.notifyUser(
                job.getJobId(),
                String.format("Job failed (attempt %d %d). Retrying in %d seconds...", newRetryCount, job.getMaxRetries())
        );
        meterRegistry.counter("job.executed", "status", "failed").increment();
    }
    private void markAsFailed(Job job, Exception e){
        job.setStatus(Job.JobStatus.FAILED);
        job.setUpdatedAt(LocalDateTime.now());
        jobRepository.save(job);

        jobFailureHandler.handleFailure(job, e.getMessage());
        notificationService.notifyUser(
                job.getJobId(),
                String.format("Job failed after %d attempts: %s", job.getMaxRetries(), e.getMessage())
        );
        meterRegistry.counter("job.executed", "status", "failed").increment();
    }

    @Transactional
    public void createExecutionLog(Job job, boolean success, String message){
        JobExecutionLog logEntry = new JobExecutionLog();
        logEntry.setJob(job);
        logEntry.setStartTime(LocalDateTime.now());
        logEntry.setEndTime(LocalDateTime.now());
        logEntry.setStatus(success ? Job.JobStatus.COMPLETED : Job.JobStatus.FAILED);
        logEntry.setExecutionMessage(message);
        jobExecutionLogRepository.save(logEntry);
        log.info("Attempting to save job execution log for job {}", job.getJobId());
    }
}
