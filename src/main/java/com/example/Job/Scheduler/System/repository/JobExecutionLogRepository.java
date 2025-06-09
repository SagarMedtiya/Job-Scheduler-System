package com.example.Job.Scheduler.System.repository;

import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.entity.JobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, UUID> {
    List<JobExecutionLog> findByJob_JobId(UUID jobId);
    Long countByJob_JobIdAndStatus(UUID jobId, Job.JobStatus  status);
}
