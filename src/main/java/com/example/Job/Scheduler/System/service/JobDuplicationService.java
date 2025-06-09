package com.example.Job.Scheduler.System.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobDuplicationService {
//    private final RedisTemplate<String, Object> redisTemplate;
//    private static final String JOB_DEDUP_PREFIX = "job:dedup:";
//    private static final Duration DEDUP_WINDOW = Duration.ofMinutes(5);
//
//    public boolean shouldProcessJob(UUID jobId) {
//        String key = JOB_DEDUP_PREFIX + jobId;
//        Boolean set = redisTemplate.opsForValue().setIfAbsent(key, "processing", DEDUP_WINDOW);
//        return set != null && set;
//    }
//
//    public void clearJobProcessing(UUID jobId) {
//        redisTemplate.delete(JOB_DEDUP_PREFIX + jobId);
//    }
}
