package com.echovault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EchoVaultApplication {
    public static void main(String[] args) {
        SpringApplication.run(EchoVaultApplication.class, args);
    }
}
