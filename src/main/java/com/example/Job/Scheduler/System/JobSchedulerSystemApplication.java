package com.example.Job.Scheduler.System;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class JobSchedulerSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobSchedulerSystemApplication.class, args);
	}

}
