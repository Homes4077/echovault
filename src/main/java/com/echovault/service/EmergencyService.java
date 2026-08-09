package com.echovault.service;

import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmergencyService {

    private final UserRepository userRepository;

    public void saveSecurityProtocol(String username, String question, String answer) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username)));
        user.setSecurityQuestion(question);
        user.setSecurityAnswer(answer);
        userRepository.save(user);
    }

    public boolean verifyEmergencyAccess(String username, String answer) {
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username)));
        return user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(answer);
    }

    public void updateSecurityQuestion(String username, String question, String answer) {
        saveSecurityProtocol(username, question, answer);
    }

    public boolean verifyAnswer(String username, String answer) {
        return verifyEmergencyAccess(username, answer);
    }
}
