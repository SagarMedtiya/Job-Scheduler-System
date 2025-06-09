package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.dto.LoginRequest;
import com.example.Job.Scheduler.System.dto.LoginResponse;
import com.example.Job.Scheduler.System.dto.RegisterRequest;
import com.example.Job.Scheduler.System.entity.User;
import com.example.Job.Scheduler.System.repository.UserRespository;
import com.example.Job.Scheduler.System.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import static org.springframework.http.ResponseEntity.badRequest;

@Service
public class UserService {
    @Autowired
    private  UserRespository userRespository;
    @Autowired
    private  PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtil jwtUtil;

    public ResponseEntity<?> register(RegisterRequest request, BindingResult result){
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldErrors());
        }
        if(request.getEmail() == null || request.getPassword() == null || request.getUsername() == null){
            return badRequest().body("username or password cannot be null");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        return ResponseEntity.ok(userRespository.save(user));
    }
    public LoginResponse login(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRespository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(user.getId(), user.getUsername(), token);
    }

    public User updateProfile(User userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        User user = userRespository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new UsernameNotFoundException("Username not found"));
        if(userDto.getEmail() != null){
            user.setEmail(userDto.getEmail());
        }
        if(userDto.getUsername() != null){
            user.setUsername(userDto.getUsername());
        }
        if(userDto.getPassword() != null && !userDto.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        return userRespository.save(user);
    }
}
