package com.echovault.controller;

import com.echovault.dto.AuthRequest;
import com.echovault.dto.AuthResponse;
import com.echovault.dto.RegisterRequest;
import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import com.echovault.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @GetMapping("/security-question")
    public ResponseEntity<?> getSecurityQuestion(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    String question = user.getSecurityQuestion();
                    if (question == null || question.isBlank()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("error", "No emergency security question set for this account."));
                    }
                    return ResponseEntity.ok(Map.of("question", question));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Account not found.")));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email is already registered."));
        }

        String assignedRole = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole() 
                : "ROLE_USER";

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser.getEmail());

        return ResponseEntity.ok(new AuthResponse(token, savedUser.getId(), savedUser.getEmail(), savedUser.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or credentials."));
        }

        String requestedMode = request.getViewMode() != null ? request.getViewMode().toUpperCase() : "USER";
        String effectiveRole;
        String redirectUrl;

        if ("FAMILY".equals(requestedMode) || "FAMILY_MEMBER".equals(requestedMode)) {
            String providedAnswer = request.getSecurityAnswer() != null ? request.getSecurityAnswer().trim().toLowerCase() : "";
            String storedAnswer = user.getSecurityAnswer();

            if (storedAnswer == null || storedAnswer.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "No emergency unlock answer configured for this account."));
            }

            boolean isAnswerValid;
            if (storedAnswer.startsWith("$2a$") || storedAnswer.startsWith("$2b$")) {
                isAnswerValid = passwordEncoder.matches(providedAnswer, storedAnswer);
            } else {
                isAnswerValid = storedAnswer.trim().equalsIgnoreCase(providedAnswer);
            }

            if (!isAnswerValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Incorrect emergency security answer."));
            }

            effectiveRole = "ROLE_FAMILY_MEMBER";
            redirectUrl = "/memorial.html";

        } else if ("ADMIN".equals(requestedMode)) {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password."));
            }
            if (!"ROLE_ADMIN".equalsIgnoreCase(user.getRole()) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied: Account lacks ADMIN privileges."));
            }
            effectiveRole = "ROLE_ADMIN";
            redirectUrl = "/admin/dashboard";

        } else {
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password."));
            }
            effectiveRole = "ROLE_USER";
            redirectUrl = "/dashboard.html";
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("role", effectiveRole);
        response.put("redirectUrl", redirectUrl);

        return ResponseEntity.ok(response);
    }
}
