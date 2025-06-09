package com.example.Job.Scheduler.System.dto;

import jakarta.persistence.Column;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private String password;
}
