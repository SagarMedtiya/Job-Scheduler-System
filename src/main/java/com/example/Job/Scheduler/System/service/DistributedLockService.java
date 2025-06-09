package com.example.Job.Scheduler.System.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class DistributedLockService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String LOCK_PREFIX = "lock:";
    private static final long LOCK_EXPIRE_MS = 30000;  //30 seconds

    public boolean acquireLock(String lockKey, String lockValue){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                LOCK_PREFIX + lockKey,
                lockValue,
                Duration.ofMillis(LOCK_EXPIRE_MS)
        ));
    }
    public boolean releaseLock(String lockKey, String lockValue){
        String currentValue = (String) redisTemplate.opsForValue().get(LOCK_PREFIX + lockKey);
        if(lockValue.equals(currentValue)){
            redisTemplate.delete(LOCK_PREFIX + lockKey);
            return true;
        }
        return false;
    }

}
