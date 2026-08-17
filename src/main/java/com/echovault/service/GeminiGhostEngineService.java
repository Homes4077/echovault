package com.echovault.service;

import org.springframework.stereotype.Service;

@Service
public class GeminiGhostEngineService {

    public String generateResponse(String userEmail, String prompt) {
        return "Gemini Ghost Engine response for user " + userEmail + ": " + prompt;
    }
}
