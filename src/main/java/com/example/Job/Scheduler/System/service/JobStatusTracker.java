package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobStatusTracker {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STATUS_KEY_PREFIX = "job:status";
    private static final Duration STATUS_TTL = Duration.ofHours(1);

    public void updateJobStatus(UUID jobId, Job.JobStatus status) {
        String key = STATUS_KEY_PREFIX + ":" + jobId;
        redisTemplate.opsForValue().set(key, status.toString());
        redisTemplate.expire(key, STATUS_TTL);
    }
    public Optional<Job.JobStatus> getJobStatus(UUID jobId) {
        String status =(String) redisTemplate.opsForValue().get(STATUS_KEY_PREFIX + ":" + jobId);
        if(status != null){
            return Optional.of(Job.JobStatus.valueOf(status));
        }
        return Optional.empty();
    }

}





















