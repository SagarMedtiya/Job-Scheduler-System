package com.example.Job.Scheduler.System.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Data
@Entity
@Table(name="jobs")
public class Job {


    public enum JobInterval {
        MINUTELY("0 * * * * *"),
        HOURLY  ("0 0 * * * *"),      // Every hour at :00
        DAILY   ("0 0 0 * * *"),        // Every day at midnight
        WEEKLY  ("0 0 0 * * MON"),     // Every Monday at midnight
        MONTHLY ("0 0 0 1 * *"),      // 1st day of month at midnight
        YEARLY  ("0 0 0 1 1 *");       // Every year on Jan 1
        private final String cronExpression;

        JobInterval(String cronExpression) {
            this.cronExpression = cronExpression;
        }

        public String getCronExpression() {
            return cronExpression;
        }
        public LocalDateTime calculateNextExecution(LocalDateTime currentSchedule) {
            return switch (this){
                case MINUTELY -> currentSchedule.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
                case DAILY    -> currentSchedule.plusDays(1).truncatedTo(ChronoUnit.DAYS);
                case MONTHLY  -> currentSchedule.plusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
                case YEARLY   -> currentSchedule.plusYears(1).truncatedTo(ChronoUnit.DAYS).withDayOfYear(1);
                case HOURLY   ->  currentSchedule.plusHours(1).truncatedTo(ChronoUnit.HOURS);
                case WEEKLY   -> currentSchedule.plusWeeks(1).truncatedTo(ChronoUnit.DAYS).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            };
        }
    }
    public enum JobStatus{
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED, IN_QUEUE
    }
    @Id
    @Column(columnDefinition = "BINARY(16)")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID jobId;

    private String jobCode;
    private String jobName;
    private String jobType;
    private LocalDateTime schedule;
    private boolean recurring;
    @Enumerated(EnumType.STRING)
    private JobInterval jobInterval;  //daily, weekly, monthly

    @Enumerated(EnumType.STRING)
    private JobStatus status = JobStatus.PENDING; // pending, running, completed, failed, cancelled

    private int retries = 0;
    private int maxRetries = 3;
    private LocalDateTime nextRetryAt;
    private String failureMessage;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
    private String cronExpression;

    public void setCronExpression(){
        if(this.jobInterval == null){
            throw new IllegalArgumentException("Job interval cannot be null");
        }
        this.cronExpression = this.jobInterval.getCronExpression();
        this.status = JobStatus.PENDING; // Ensure recurring jobs start as PENDING
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        // For recurring jobs that complete, reset to PENDING
        if (this.isRecurring() && this.status == JobStatus.COMPLETED) {
            this.status = JobStatus.PENDING;
        }
    }

}































