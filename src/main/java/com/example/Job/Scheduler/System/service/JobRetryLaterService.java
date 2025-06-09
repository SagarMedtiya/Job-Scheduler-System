package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;



@Service
@RequiredArgsConstructor
@Slf4j
public class JobRetryLaterService {

    private final JobRepository jobRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String JOB_RETRY_QUEUE_KEY = "jobs:retry";
    private static final String JOB_QUEUE_KEY = "jobs:queue";

    public void retryLater(UUID job){
        redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, job.toString());
    }

    @Scheduled(fixedRate =  5000) //check every 5 seconds
    public void processRetryQueue(){
        long now = System.currentTimeMillis();
        Set<Object> jobIds = redisTemplate.opsForZSet().rangeByScore(
                JOB_RETRY_QUEUE_KEY, 0 , now
        );
        if(jobIds != null && !jobIds.isEmpty()){
            jobIds.forEach(jobId -> {
                redisTemplate.opsForZSet().remove(JOB_RETRY_QUEUE_KEY, jobId);
                redisTemplate.opsForList().rightPush(JOB_QUEUE_KEY, jobId);
            });
        }
    }
}



































