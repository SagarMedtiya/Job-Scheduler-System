package com.example.Job.Scheduler.System.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description ="Request object for creating a new job")
public class JobRequest {
    @Schema(description = "Name of the job", example ="Data Processing Job")
    @NotBlank
    private String jobName;
    @Schema(description="Type of the job", example ="TEST_MINUTELY_JOB" )
    @NotBlank
    private String jobType;
    @Schema(description = "Time", example="2025-06-05T14:26:00")
    private LocalDateTime schedule;
    @Schema(description = "Recurring of the job")
    private boolean recurring;
    @Schema(description = "interval of the job", example="DAILY, WEEKLY, MONTHLY")
    @NotNull
    private String jobInterval;  //daily, weekly, monthly
}
