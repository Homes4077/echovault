package com.echovault.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class GeminiGhostEngineService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String queryGhostEngine(String vaultOwnerName, List<String> taggedContent, String userQuery) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String groundTruthMemories = String.join("\n- ", taggedContent);

        // Strict system instruction preventing hallucination/fabrication
        String systemInstruction = String.format(
            "You are the EchoVault Ghost Assistant representing the preserved words of %s. " +
            "CRITICAL DIRECTIVE: Answer strictly using ONLY the provided vault entries below. " +
            "If the question cannot be answered using these entries, reply EXACTLY: " +
            "'I do not have a recorded memory regarding this topic in my vault.' " +
            "Never invent, assume, or fabricate any detail outside this text.\n\n" +
            "STORED VAULT ENTRIES:\n- %s",
            vaultOwnerName, groundTruthMemories
        );

        Map<String, Object> requestPayload = Map.of(
            "system_instruction", Map.of("parts", List.of(Map.of("text", systemInstruction))),
            "contents", List.of(Map.of("parts", List.of(Map.of("text", userQuery))))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestPayload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            List candidates = (List) response.getBody().get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            Map firstPart = (Map) parts.get(0);
            return (String) firstPart.get("text");
        } catch (Exception e) {
            return "Unable to consult the Ghost Engine context at this time.";
        }
    }
}
