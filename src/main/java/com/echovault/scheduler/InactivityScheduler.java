package com.echovault.scheduler;

import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InactivityScheduler {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 12 * * ?")
    public int executeInactivityChecks() {
        List<User> users = userRepository.findAll();
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        int flaggedCount = 0;

        for (User user : users) {
            if (user.getLastLoginAt() != null && user.getLastLoginAt().isBefore(threshold)) {
                // Execute emergency / inactivity trigger workflow
                flaggedCount++;
            }
        }

        return flaggedCount;
    }
}
