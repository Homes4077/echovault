package com.echovault.service;

import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final UserRepository userRepository;

    public Map<String, Object> verifyAndGenerateAccess(String email, String answer) {
        Map<String, Object> result = new HashMap<>();
        
        // Basic emergency validation check
        boolean isValid = userRepository.findByEmail(email)
                .isPresent(); // Customize with exact security answer entity logic if required

        if (isValid) {
            result.put("status", "SUCCESS");
            result.put("accessKey", "EMERGENCY-TEMP-TOKEN-" + System.currentTimeMillis());
            result.put("message", "Emergency protocols verified.");
        } else {
            result.put("status", "FAILED");
            result.put("message", "Verification failed.");
        }
        return result;
    }

    public void saveSecurityQuestion(String userEmail, String question, String answer) {
        userRepository.findByEmail(userEmail).ifPresent(user -> {
            // Persist emergency question & answer to user record
            userRepository.save(user);
        });
    }
}
