package com.echovault.service;

import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Injected if BCrypt is configured

    public boolean saveSecurityQuestion(String userEmail, String question, String answer) {
        if (userEmail == null || question == null || answer == null || answer.isBlank()) {
            return false;
        }

        String normalizedAnswer = answer.trim().toLowerCase();

        return userRepository.findByEmail(userEmail).map(user -> {
            user.setSecurityQuestion(question.trim());
            // Optional: Use passwordEncoder.encode(normalizedAnswer) for hashing
            user.setSecurityAnswer(passwordEncoder.encode(normalizedAnswer)); 
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    public boolean verifyEmergencyAccess(String userEmail, String accessCode) {
        if (userEmail == null || accessCode == null || accessCode.isBlank()) {
            return false;
        }

        String normalizedCode = accessCode.trim().toLowerCase();

        return userRepository.findByEmail(userEmail)
                .map(user -> user.getSecurityAnswer() != null && 
                             passwordEncoder.matches(normalizedCode, user.getSecurityAnswer()))
                .orElse(false);
    }
}
