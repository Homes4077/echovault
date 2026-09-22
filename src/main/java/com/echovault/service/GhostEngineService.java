package com.echovault.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GhostEngineService {

    private final GeminiGhostEngineService geminiGhostEngineService;

    /**
     * Delegates prompt processing to the Gemini Ghost Engine service.
     *
     * @param userEmail Email or identifier of the memory owner
     * @param prompt    User prompt or query
     * @return Formatted AI response or graceful error fallback
     */
    public String processQuery(String userEmail, String prompt) {
        if (userEmail == null || userEmail.isBlank()) {
            return "Please provide a valid user email to access memory vaults.";
        }

        try {
            return geminiGhostEngineService.generateResponse(userEmail, prompt);
        } catch (Exception e) {
            log.error("Error processing Ghost Engine query for user [{}]: {}", userEmail, e.getMessage(), e);
            return "I am having trouble connecting to my memory vault at the moment. Please try again shortly.";
        }
    }
}