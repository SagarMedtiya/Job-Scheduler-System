package com.example.Job.Scheduler.System.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    private final RedisTemplate<String, Object> redisTemplate;

    public boolean isRateLimited(String key, int maxRequests, Duration duration ){
        String redisKey = "rate_limit:" + key;
        try {
            Long count = redisTemplate.opsForValue().increment(redisKey);

            if (count != null && count == 1) {
                redisTemplate.expire(redisKey, duration);
            }

            boolean limited = count != null && count > maxRequests;
            if (limited) {
                log.warn("Rate limit exceeded for key: {}", key);
            }

            return limited;
        } catch (Exception e) {
            log.error("Rate limiting failed for key: {}", key, e);
            return false; // fallback to allow if Redis is down
        }
    }

}
