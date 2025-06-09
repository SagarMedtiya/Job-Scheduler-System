package com.example.Job.Scheduler.System.security;

import com.example.Job.Scheduler.System.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    private final Set<String> blacklistedToken = ConcurrentHashMap.newKeySet();
    private JwtUtil jwtUtil;

    public TokenBlacklist(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    public void addToBlacklist(String token) {
        blacklistedToken.add(token);
    }
    public boolean isBlacklisted(String token) {
        return blacklistedToken.contains(token);
    }
    @Scheduled(fixedDelay = 5000)
    public void removeExpiredTokens(){
        blacklistedToken.removeIf(token ->{
            try{
                Claims claims = jwtUtil.extractAllClaims(token);
                return claims.getExpiration().before(new Date());
            }
            catch(Exception e){
                return true;
            }
        });
    }
}
