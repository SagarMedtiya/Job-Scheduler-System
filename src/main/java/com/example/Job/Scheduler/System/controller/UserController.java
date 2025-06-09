package com.example.Job.Scheduler.System.controller;

import com.example.Job.Scheduler.System.dto.LoginRequest;
import com.example.Job.Scheduler.System.dto.RegisterRequest;
import com.example.Job.Scheduler.System.entity.User;
import com.example.Job.Scheduler.System.repository.UserRespository;
import com.example.Job.Scheduler.System.security.TokenBlacklist;
import com.example.Job.Scheduler.System.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TokenBlacklist tokenBlacklist;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/auth/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        System.out.println("Received User: " + request);
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors());
        }
        return ResponseEntity.ok(userService.register(request, result));
    }
    @Operation(summary = "Authenticate user", description = "Returns JWT token for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/auth/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
    //update the users details
    @PutMapping("/users/me")
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.updateProfile(user));
    }
}
