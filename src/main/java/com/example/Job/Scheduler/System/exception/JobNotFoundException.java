package com.example.Job.Scheduler.System.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message){
        super(message);
    }
}
