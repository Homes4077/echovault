package com.echovault.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GhostEngineService {

    private final GeminiGhostEngineService geminiGhostEngineService;

    public String processQuery(String userEmail, String prompt) {
        try {
            return geminiGhostEngineService.generateResponse(userEmail, prompt);
        } catch (Exception e) {
            System.err.println("Error processing Ghost Engine Query: " + e.getMessage());
            return "I am having trouble connecting to my memory vault at the moment. Please try again shortly.";
        }
    }
}
