package com.example.Job.Scheduler.System.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private Long id;
    private String username;
    private String jwtToken;
}
