package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobRecoveryService {
    private final JobRepository jobRepository;
    private final JobExecutor jobExecutor;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String JOB_QUEUE_KEY = "jobs:queue";

    @PostConstruct
    @Transactional
    public void recoverInterruptedJobs(){
        log.info("Running job recovery on startup...");

        //Re-enqueue pending jobs that were in progress
        List<Job> pendingJobs = jobRepository.findByStatusAndNextRetryAtBefore(Job.JobStatus.PENDING, LocalDateTime.now());
        pendingJobs.forEach(job ->{
            if(job.getNextRetryAt() == null || LocalDateTime.now().isAfter(job.getNextRetryAt())){
                redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, job.getJobCode().toString());
                log.info("Re-enqueued pending job {}", job.getJobId().toString());
            }
        });
        //Rebuild retry queue from database
        List<Job> failedJobs = jobRepository.findByStatusAndRetryCountLessThan(
                Job.JobStatus.FAILED);

        failedJobs.forEach(job ->{
            job.setStatus(Job.JobStatus.PENDING);
            jobRepository.save(job);
            redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, job.getJobId().toString());
            log.info("Recovered failed job {} for retry", job.getJobId());
        });
    }

}
































