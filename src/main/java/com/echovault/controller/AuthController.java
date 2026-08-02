package com.echovault.controller;

import com.echovault.dto.SecurityUnlockDto;
import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/emergency-unlock")
    public String handleEmergencyUnlock(@ModelAttribute SecurityUnlockDto dto, Model model) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Account not found.");
            return "emergency-unlock";
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            model.addAttribute("error", "Account locked due to 3 failed attempts. Try again later.");
            return "emergency-unlock";
        }

        boolean matches = passwordEncoder.matches(dto.getSecurityAnswer().toLowerCase().trim(), user.getSecurityAnswerHash());

        if (matches) {
            user.setFailedUnlockAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
            model.addAttribute("success", "Emergency unlock verified! Access granted.");
            return "redirect:/memorial/" + user.getId();
        } else {
            int attempts = user.getFailedUnlockAttempts() + 1;
            user.setFailedUnlockAttempts(attempts);
            if (attempts >= 3) {
                user.setLockedUntil(LocalDateTime.now().plusHours(24));
            }
            userRepository.save(user);
            model.addAttribute("error", "Incorrect answer. Attempt " + attempts + " of 3.");
            return "emergency-unlock";
        }
    }
}
