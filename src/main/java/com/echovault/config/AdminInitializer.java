package com.echovault.config;

import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL = "liljaymar254@gmail.com";
    private static final String ADMIN_RAW_PASSWORD = "jaymar#";

    @Override
    public void run(String... args) {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
            admin -> {
                // Keep password and role synchronized on every restart
                admin.setPassword(passwordEncoder.encode(ADMIN_RAW_PASSWORD));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
            },
            () -> {
                User newAdmin = User.builder()
                        .fullName("System Administrator")
                        .email(ADMIN_EMAIL)
                        .password(passwordEncoder.encode(ADMIN_RAW_PASSWORD))
                        .role("ROLE_ADMIN")
                        .lastLoginAt(LocalDateTime.now())
                        .build();
                userRepository.save(newAdmin);
            }
        );
    }
}
