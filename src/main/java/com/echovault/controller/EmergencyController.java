package com.echovault.controller;

import com.echovault.security.JwtTokenProvider;
import com.echovault.service.EmergencyService;
import com.echovault.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    private final EmergencyService emergencyService;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public EmergencyController(EmergencyService emergencyService, 
                               JwtTokenProvider tokenProvider, 
                               UserRepository userRepository) {
        this.emergencyService = emergencyService;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/recovery-question")
    public ResponseEntity<?> saveRecoveryQuestion(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized access."));
        }

        String userEmail = principal.getName();
        String question = payload.get("question");
        String answer = payload.get("answer");

        if (question == null || question.isBlank() || answer == null || answer.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Both question and answer are required."));
        }

        boolean success = emergencyService.saveSecurityQuestion(userEmail, question, answer);

        if (!success) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User account not found."));
        }

        return ResponseEntity.ok(Map.of("message", "Emergency protocol saved successfully!"));
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> verifyAndUnlock(@RequestBody Map<String, String> payload) {
        String email = payload.get("email") != null ? payload.get("email") : payload.get("userEmail");
        
        String answer = payload.get("answer");
        if (answer == null) answer = payload.get("accessCode");
        if (answer == null) answer = payload.get("securityAnswer");

        if (email == null || email.isBlank() || answer == null || answer.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Both email and security answer are required."));
        }

        boolean isUnlocked = emergencyService.verifyEmergencyAccess(email, answer);

        if (!isUnlocked) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Emergency unlock failed: Invalid answer or account."));
        }

        String familyToken = tokenProvider.generateToken(email, "ROLE_FAMILY_MEMBER");

        String ownerName = userRepository.findByEmail(email.trim().toLowerCase())
                .map(u -> u.getFullName() != null ? u.getFullName() : u.getName())
                .orElse("Vault Owner");

        Map<String, Object> response = new HashMap<>();
        response.put("token", familyToken);
        response.put("role", "ROLE_FAMILY_MEMBER");
        response.put("ownerName", ownerName);
        response.put("message", "Emergency family access granted!");

        return ResponseEntity.ok(response);
    }
}
