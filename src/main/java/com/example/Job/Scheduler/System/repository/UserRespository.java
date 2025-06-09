package com.example.Job.Scheduler.System.repository;

import com.example.Job.Scheduler.System.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRespository extends JpaRepository<User, Long> {
    Optional <User> findByUsername(String username);
}
