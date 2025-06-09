package com.example.Job.Scheduler.System.util;

import com.example.Job.Scheduler.System.service.NotificationService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        try{
            Map<String, String> notification = objectMapper.readValue(message.getBody(), new TypeReference<HashMap<String, String>>() {});
            notificationService.notifyUser(
                    UUID.fromString(notification.get("jobId")),
                    notification.get("message")
            );
        }catch(Exception e){
                log.error("Error processing notification", e);
        }
    }
}
