package com.example.Job.Scheduler.System.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username cannot be empty")
    private String Username;
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Column(nullable = false)
    private String Password;
    @Column(nullable = false)
    private String Email;
}
