package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.dto.JobRequest;
import com.example.Job.Scheduler.System.dto.JobResponse;
import com.example.Job.Scheduler.System.entity.Job;
import com.example.Job.Scheduler.System.repository.JobExecutionLogRepository;
import com.example.Job.Scheduler.System.repository.JobRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class JobService  {
        private final JobRepository jobRepository;
        private final JobExecutionLogRepository logRepository;
        private final NotificationService notificationService;
        private final JobExecutor jobExecutor;
        private final MeterRegistry meterRegistry;
        private final JobExecutionService jobExecutionService;
        private final Clock clock;

        @Autowired
        @Lazy // Prevent circular dependency if this is part of a cycle
        private JobSchedulingService jobSchedulingService;

        @CacheEvict(value ="jobs", key ="#job.jobId")
        public Job saveJob(Job job){
            return jobRepository.save(job);
        }


        //submit a job
        @Transactional
        public Job submitJob(JobRequest jobRequest) {
            //validate the input first
            if(jobRequest == null){
                throw new IllegalArgumentException("jobRequest cannot be null");
            }

            Job job = new Job();
            job.setJobCode(generateJobId());
            job.setJobName(jobRequest.getJobName());
            job.setJobType(jobRequest.getJobType());
            job.setSchedule(jobRequest.getSchedule());
            job.setRecurring(jobRequest.isRecurring());
            try{
                job.setJobInterval(Job.JobInterval.valueOf(jobRequest.getJobInterval().toUpperCase()));
            }catch(IllegalArgumentException e){
                throw new IllegalArgumentException("Invalid job interval" + jobRequest.getJobInterval());
            }

            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());

            LocalDateTime now = LocalDateTime.now(clock);
            if(job.isRecurring()) {
                job.setSchedule(
                        job.getJobInterval().calculateNextExecution(LocalDateTime.now())
                );
                job.setCronExpression(); // This also sets status to PENDING
                job.setStatus(Job.JobStatus.PENDING);
            }
            else{
                if(jobRequest.getSchedule() == null){
                    throw new IllegalArgumentException("schedule must be provided for non-recurring jobs");
                }
                job.setSchedule(jobRequest.getSchedule());
            }
            // Enqueue immediately if scheduled in the past/present
            if (!job.getSchedule().isAfter(now)) {
                jobExecutor.enqueueJob(job.getJobId());
            }
            return  saveJob(job);


        }
        public String generateJobId() {
            return "JOB-" + (int) (Math.random() * 100000); // Example: JOB-12345
        }

        //get a job by id
//        @Cacheable(value = "jobs", key ="#jobId", cacheManager = "cacheManager")
        public JobResponse getJobById(String jobId) {
            Job savedJob = jobRepository.findByJobCode(jobId).orElseThrow(()-> new RuntimeException("Job not found"));
            return convertToResponse(savedJob);
        }

        // cancel a job
        @Transactional
        public void cancelJob(String jobId) {
            Job job = jobRepository.findByJobCode(jobId).orElseThrow(()-> new RuntimeException("Job not found"));
            if(job.getStatus() == Job.JobStatus.COMPLETED || job.getStatus() == Job.JobStatus.FAILED){
                throw new RuntimeException("Cannot cancel a job that is already completed or failed");
            }
            job.setStatus(Job.JobStatus.CANCELLED);
            jobRepository.save(job);
        }

        //reschedule a job
        public JobResponse rescheduleJob(String jobId, LocalDateTime newTime) {
            if(newTime.isBefore(LocalDateTime.now())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New schedule time must be in the future");
            }
            Job job = jobRepository.findByJobCode(jobId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
            if(job.getStatus() == Job.JobStatus.COMPLETED || job.getStatus() == Job.JobStatus.FAILED || job.getStatus() == Job.JobStatus.CANCELLED){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reschedule a job that is already completed or failed");
            }
            job.setSchedule(newTime);
            job.setStatus(Job.JobStatus.PENDING);
            Job savedJob = jobRepository.save(job);
            return convertToResponse(savedJob);
        }

        //get all jobs
        @Transactional(readOnly = true)
        public Page<JobResponse> getAllJobs(Pageable pageable) {
            return jobRepository.findAll(pageable).map(this::convertToResponse);
        }

        private JobResponse convertToResponse(Job job) {
            return new JobResponse(
                    job.getJobId(),
                    job.getJobName(),
                    job.getJobType(),
                    job.getSchedule(),
                    job.isRecurring(),
                    job.getJobInterval(),
                    job.getStatus().name(),
                    job.getCreatedAt(),
                    job.getUpdatedAt(),
                    job.getJobCode()
            );
        }
        //Execute job
        @Async("taskExecutor")
        public void executeJob(UUID jobId) {
            jobExecutor.executeJob(jobId);
        }

        //get the status of the job
        @Transactional(readOnly = true)
        public JobResponse getJobStatus(String jobId) {
            Job job = jobRepository.findByJobCode(jobId).orElseThrow(()-> new RuntimeException("Job not found"));
            return new JobResponse(
                    job.getJobId(),
                    job.getJobName(),
                    job.getJobCode(),
                    job.getStatus()
            );
        }
    }