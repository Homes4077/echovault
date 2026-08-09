package com.echovault.controller;

import com.echovault.service.EmergencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @PostMapping("/setup")
    public ResponseEntity<?> setupProtocol(@RequestBody Map<String, String> payload, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        String question = payload.get("question");
        String answer = payload.get("answer");

        if (question == null || answer == null || question.isBlank() || answer.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question and answer are required"));
        }

        emergencyService.saveSecurityProtocol(principal.getName(), question, answer);
        return ResponseEntity.ok(Map.of("message", "Security protocol saved successfully"));
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> verifyUnlock(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String answer = payload.get("answer");

        boolean isUnlocked = emergencyService.verifyEmergencyAccess(email, answer);
        if (isUnlocked) {
            return ResponseEntity.ok(Map.of("message", "Emergency access granted"));
        } else {
            return ResponseEntity.status(401).body(Map.of("error", "Incorrect security answer"));
        }
    }
}
