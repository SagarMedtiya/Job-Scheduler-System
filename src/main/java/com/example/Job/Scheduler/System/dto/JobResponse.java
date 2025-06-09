package com.example.Job.Scheduler.System.dto;

import com.example.Job.Scheduler.System.entity.Job;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {
    private UUID jobID;
    private String jobName;
    private String JobType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime schedule;
    private boolean recurring;
    private Job.JobInterval jobInterval;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private String id;

    public JobResponse(UUID jobId, String jobName, String id, Job.JobStatus status) {
        this.jobID = jobId;
        this.jobName = jobName;
        this.id = id;
        this.status = status.toString();

    }
}
