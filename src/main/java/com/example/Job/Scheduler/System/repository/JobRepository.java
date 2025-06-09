package com.example.Job.Scheduler.System.repository;

import com.example.Job.Scheduler.System.entity.Job;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT j FROM Job j WHERE j.jobId = :jobId")
    Optional<Job> findByIdWithLock(@Param("jobId") UUID jobId);


    Optional<Job> findByJobCode( String jobCode);

    List<Job> findByRecurringAndStatusAndScheduleLessThanEqual(
            boolean recurring, Job.JobStatus status, LocalDateTime schedule);

    long countByStatus(Job.JobStatus status);

    List<Job> findByStatus(Job.JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.retries < j.maxRetries")
    List<Job> findByStatusAndRetryCountLessThan(@Param("status") Job.JobStatus status);

    List<Job> findByStatusAndNextRetryAtBefore(Job.JobStatus jobStatus, LocalDateTime now);
}
