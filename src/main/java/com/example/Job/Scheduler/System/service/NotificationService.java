package com.example.Job.Scheduler.System.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {
    //Simulate sending a notification
    public void notifyUser(UUID jobId, String message){
        System.out.println("Notification sent for job" + jobId + ": " + message);
    }
}
