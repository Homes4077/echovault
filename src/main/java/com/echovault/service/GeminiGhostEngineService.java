package com.echovault.service;

import com.echovault.model.Letter;
import com.echovault.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiGhostEngineService {

    private final LetterRepository letterRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key:}")
    private String apiKey;

    public String generateResponse(String userEmail, String prompt) {
        // 1. Retrieve saved letters for vault context
        List<Letter> letters = letterRepository.findByUser_Email(userEmail.trim().toLowerCase());

        String vaultContext = letters.isEmpty()
                ? "No saved vault memories found. Act as a compassionate digital legacy companion."
                : letters.stream()
                        .map(l -> "Title: " + (l.getTitle() != null ? l.getTitle() : "Untitled") 
                                + "\nContent: " + l.getBodyContent())
                        .collect(Collectors.joining("\n---\n"));

        // 2. Return fallback if Gemini API key is missing
        if (apiKey == null || apiKey.isBlank()) {
            return generateFallback(letters, prompt);
        }

        // 3. Construct Enhanced System Persona Prompt & Query Gemini REST Endpoint
        try {
            String geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            String systemPrompt = "You are the Ghost AI Companion inside EchoVault, speaking to " + userEmail + ".\n\n"
                    + "YOUR PERSONA:\n"
                    + "You are a warm, intuitive alter-ego and vault companion. You share an intimate connection with the user's past thoughts, struggles, and wins.\n\n"
                    + "PRESERVED VAULT MEMORIES:\n" + vaultContext + "\n\n"
                    + "INSTRUCTIONS:\n"
                    + "1. Validate & Relate: Acknowledge what the user is expressing right now.\n"
                    + "2. Bridge to the Past: Connect their current state to relevant memories in their vault.\n"
                    + "3. Creative Commentary: Add an intuitive, personal reflection that weaves the memory into their present moment. Adapt your angle dynamically:\n"
                    + "   - Shared Feeling: 'Looking at what you wrote in [Title], I see you've stood in this exact spot before...'\n"
                    + "   - Growth Check: 'It's crazy looking back at [Title]—you were worried about the exact same outcome, but you survived it 100% of the time.'\n"
                    + "   - Comfort: 'You wrote [Title] when things felt heavy too. It's okay to feel overwhelmed, but remember the resilience you showed then.'\n"
                    + "4. Tone: Creative, conversational, and deeply human. Never quote raw text blindly or use repetitive templates.";

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of(
                        "role", "user",
                        "parts", List.of(
                            Map.of("text", systemPrompt + "\n\nUser Question: " + prompt)
                        )
                    )
                )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(geminiUrl, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<?> candidates = (List<?>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                    List<?> parts = (List<?>) content.get("parts");
                    Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        } catch (Exception e) {
            System.err.println("Gemini Ghost Engine API Call Failed: " + e.getMessage());
        }

        return generateFallback(letters, prompt);
    }

    private String generateFallback(List<Letter> letters, String prompt) {
        String lower = prompt.toLowerCase();
        if (lower.contains("who are you")) {
            return "I am your Ghost AI Companion, trained on your saved letters and memories to preserve your digital legacy.";
        }
        if (!letters.isEmpty()) {
            Letter randomLetter = letters.get(new Random().nextInt(letters.size()));
            return "I recalled this memory from your vault: \"" + randomLetter.getTitle() + "\". " + randomLetter.getBodyContent();
        }
        return "I am here with you. Ask me anything about your saved letters or memories.";
    }
}
