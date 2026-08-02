package com.echovault.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class AsyncConfig {
    // Enables Spring @Scheduled background jobs and asynchronous API processing
}
