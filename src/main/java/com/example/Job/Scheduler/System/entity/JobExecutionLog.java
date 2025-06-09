package com.example.Job.Scheduler.System.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_execution_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID logId;

    @ManyToOne
    @JoinColumn(name ="job_id")
    private Job job;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    @Enumerated(EnumType.STRING)
    private Job.JobStatus status; // SUCCESS, FAILED, RETRY
    private String executionMessage;
    private long durationMillis;
    private String hostname;

    @PrePersist
    @PreUpdate
    public void calculateDuration(){
        if(startTime != null && endTime != null){
            this.durationMillis = Duration.between(startTime, endTime).toMillis();
        }
    }

}






























