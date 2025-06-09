package com.example.Job.Scheduler.System.controller;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.entity.JobExecutionLog;
import com.example.Job.Scheduler.System.repository.JobExecutionLogRepository;
import com.example.Job.Scheduler.System.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.impl.SchedulerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final JobRepository jobRepository;
    private final JobExecutionLogRepository logRepository;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth(){
        Map<String, Object> health = new HashMap<>();
        health.put("status", "OK");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/jobs/stats")
    public ResponseEntity<Map<String, Object>> getJobStats(){
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", jobRepository.count());
        stats.put("pendingJobs", jobRepository.countByStatus(Job.JobStatus.PENDING));
        stats.put("runningJobs", jobRepository.countByStatus(Job.JobStatus.RUNNING));
        stats.put("failedJobs", jobRepository.countByStatus(Job.JobStatus.FAILED));
        return ResponseEntity.ok(stats);
    }
    @GetMapping("/jobs/{jobId}/history")
    public ResponseEntity<List<JobExecutionLog>> getJobHistory(@PathVariable UUID jobId){
        return ResponseEntity.ok(logRepository.findByJob_JobId(jobId));
    }
}


























