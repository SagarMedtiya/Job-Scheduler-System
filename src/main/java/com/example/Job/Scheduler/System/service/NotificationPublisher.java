package com.example.Job.Scheduler.System.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String NOTIFICATION_CHANNEL = "notifications";

    public void publishJobNotification(UUID jobId, String message){
        Map<String, String> notification = Map.of(
                "jobId", jobId.toString(),
                "message", message,
                "timestamp" , LocalDateTime.now().toString()
        );
        redisTemplate.convertAndSend(NOTIFICATION_CHANNEL, notification);
    }
}
