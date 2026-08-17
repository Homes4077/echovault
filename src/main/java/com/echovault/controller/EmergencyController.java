package com.echovault.controller;

import com.echovault.dto.SecurityUnlockDto;
import com.echovault.service.EmergencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping("/unlock")
    public ResponseEntity<?> emergencyUnlock(@RequestBody SecurityUnlockDto unlockDto) {
        Map<String, Object> result = emergencyService.verifyAndGenerateAccess(unlockDto.getEmail(), unlockDto.getAnswer());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/recovery-question")
    public ResponseEntity<?> configureRecoveryQuestion(@RequestBody SecurityUnlockDto dto, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized access."));
        }

        emergencyService.saveSecurityQuestion(principal.getName(), dto.getQuestion(), dto.getAnswer());
        
        // Return explicit JSON object to avoid "Unexpected end of JSON Input" on frontend
        return ResponseEntity.ok(Map.of("message", "Emergency security protocol saved successfully."));
    }
}
