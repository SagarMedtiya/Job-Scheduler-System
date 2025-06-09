package com.example.Job.Scheduler.System.controller;

import com.example.Job.Scheduler.System.dto.JobRequest;
import com.example.Job.Scheduler.System.dto.JobResponse;
import com.example.Job.Scheduler.System.dto.RescheduleRequest;
import com.example.Job.Scheduler.System.service.JobService;
import com.example.Job.Scheduler.System.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/jobs")
@Tag(name="JOb Controller", description =" API For scheduling and managing background jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private final RateLimitService rateLimitService;


    public JobController(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    /**
     * Submit a job
     * @param job job creation request
     * @param request HTTP request for rate limiting
     * @return created JOB
     */
    @Operation(summary = "Submit a new job", description = "Create and schedule a new job")
    @ApiResponses(value = {
            @ApiResponse(responseCode ="200", description ="Job created successfully"),
            @ApiResponse(responseCode = "429", description = "Too many request")
    })
    @PostMapping
    public ResponseEntity<?> submitJob(@RequestBody JobRequest job, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        if(rateLimitService.isRateLimited(clientIp, 10, Duration.ofMinutes(1))){
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        return ResponseEntity.ok(jobService.submitJob(job));
    }

    /**
     * Get all jobs with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of job
     */
    @Operation(summary = "Get all jobs", description = "Retrieve a list of all jobs")
    @GetMapping
    public Page<JobResponse> getAllJobs(@PageableDefault(size = 20) Pageable pageable) {
        return jobService.getAllJobs(pageable);
    }

    /**
     * Get a specific job by ID
     * @param jobId JOb ID
     * @return Job details
     */
    @Operation(summary = "Get job by ID", description = "Retrieve a specific job by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Job found"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
    @GetMapping("/{jobId}")
    public JobResponse getJob(@Parameter(description = "ID of the job to retrieve")@PathVariable String jobId) {
        return jobService.getJobById(jobId);
    }

    /**
     * Get job status by ID
     * @param jobId JOb ID
     * @return Job status response
     */
    @Operation(summary = "Get job status", description = "Get the current status of a job")
    @ApiResponses(value ={
            @ApiResponse(responseCode = "200", description = "Status retrieved"),
            @ApiResponse(responseCode = "404", description = "Job not found")
    })
        @GetMapping("/{jobId}/status")
    public JobResponse getJobStatus(@PathVariable String jobId){
        return jobService.getJobStatus(jobId);
    }

    /**
     * Cancel a job by ID
     * @param jobId Job ID to cancel
     * @return No contact response
     */
    @Operation(summary = "Cancel a job", description = "Cancel a scheduled job its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204" , description = "job cancelled successfully"),
            @ApiResponse(responseCode = "404", description="Job not found")
    })
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> cancelJob(@PathVariable String jobId){
        jobService.cancelJob(jobId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reschedule a job
     *
     * @param jobId       Job ID to reschedule
     * @param newSchedule New schedule time
     * @return Updated job
     */
    @Operation(summary = "Reschdule a job", description = "Change the schedule time for an existing job")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description ="Job rescheduled successfully"),
            @ApiResponse(responseCode = "404" , description = "Job not found")
    })
    @PutMapping("/{jobId}/reschedule")
    public JobResponse rescheduleJob(@PathVariable String jobId, @RequestBody RescheduleRequest newSchedule) {
        return jobService.rescheduleJob(jobId, newSchedule.getNewSchedule());
    }
//    /**
//     * Search jobs by name
//     * @param Id Job name (partial match)
//     * @return List of matching jobs
//     */
//    @GetMapping("/search")
//    public List<Job> searchJobsById(@RequestParam String Id){
//        return jobService.searchJobsByName(Id);
//    }
}














