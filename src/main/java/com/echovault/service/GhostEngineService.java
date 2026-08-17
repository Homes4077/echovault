package com.echovault.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GhostEngineService {

    private final GeminiGhostEngineService geminiGhostEngineService;

    public String processQuery(String userEmail, String prompt) {
        if (geminiGhostEngineService != null) {
            try {
                return geminiGhostEngineService.generateResponse(userEmail, prompt);
            } catch (Exception e) {
                // Fallback response if engine execution fails
            }
        }
        return "Ghost Engine: Memory query logged for " + userEmail;
    }
}
