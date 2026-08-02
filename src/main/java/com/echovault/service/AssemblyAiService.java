package com.echovault.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class AssemblyAiService {

    @Value("${assemblyai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String transcribeFromUrl(String audioUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of("audio_url", audioUrl);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
            "https://api.assemblyai.com/v2/transcript", entity, Map.class
        );

        String transcriptId = (String) response.getBody().get("id");
        return pollTranscriptResult(transcriptId);
    }

    private String pollTranscriptResult(String transcriptId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String pollUrl = "https://api.assemblyai.com/v2/transcript/" + transcriptId;

        while (true) {
            ResponseEntity<Map> response = restTemplate.exchange(pollUrl, HttpMethod.GET, entity, Map.class);
            String status = (String) response.getBody().get("status");

            if ("completed".equalsIgnoreCase(status)) {
                return (String) response.getBody().get("text");
            } else if ("error".equalsIgnoreCase(status)) {
                return "TRANSCRIPTION_FAILED";
            }

            try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}
