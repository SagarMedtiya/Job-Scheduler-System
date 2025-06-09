package com.example.Job.Scheduler.System.service;

import com.example.Job.Scheduler.System.entity.User;
import com.example.Job.Scheduler.System.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsServiceImp implements UserDetailsService {

    @Autowired
    private UserRespository userRespository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = userRespository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        return new User(user.getUsername(), user.getPassword());
    }
}
