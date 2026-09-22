package com.echovault.controller;

import com.echovault.model.Tag;
import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voice-notes")
public class VoiceNoteController {

    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;

    @Value("${assemblyai.api.key:${ASSEMBLYAI_API_KEY:}}")
    private String assemblyApiKey;

    public VoiceNoteController(VoiceNoteRepository voiceNoteRepository, UserRepository userRepository) {
        this.voiceNoteRepository = voiceNoteRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Unauthorized user session.");
        }
        String identifier = auth.getName();
        return userRepository.findByEmail(identifier)
                .orElseGet(() -> userRepository.findByUsernameOrEmail(identifier)
                        .orElseThrow(() -> new RuntimeException("User not found: " + identifier)));
    }

    @PostMapping
    public ResponseEntity<?> saveVoiceNote(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tag", required = false) String tagStr,
            @RequestParam(value = "contextTag", required = false) String contextTagStr,
            @RequestParam(value = "transcript", required = false) String userTranscript,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            User user = getAuthenticatedUser();

            // Default title if missing
            if (title == null || title.isBlank()) {
                title = "Voice Note " + LocalDateTime.now().toString().substring(0, 16).replace("T", " ");
            }

            // Determine tag from either 'tag' or 'contextTag'
            String selectedTagStr = (tagStr != null && !tagStr.isBlank()) ? tagStr : contextTagStr;
            Tag tagEnum;
            try {
                tagEnum = (selectedTagStr != null) ? Tag.valueOf(selectedTagStr.toUpperCase()) : Tag.MOTIVATIONAL;
            } catch (IllegalArgumentException | NullPointerException e) {
                tagEnum = Tag.MOTIVATIONAL;
            }

            String finalTranscript = "";

            // 1. Prioritize transcript entered manually on frontend
            if (userTranscript != null && !userTranscript.trim().isEmpty()) {
                finalTranscript = userTranscript.trim();
            }
            // 2. Fall back to backend AssemblyAI transcription if audio file is provided
            else if (file != null && !file.isEmpty() && assemblyApiKey != null && !assemblyApiKey.isBlank() && !"dummy_assemblyai_key".equals(assemblyApiKey.trim())) {
                finalTranscript = transcribeWithAssemblyAI(file.getBytes());
            }

            // 3. Fallback string if all transcription attempts remain empty
            if (finalTranscript.isBlank()) {
                finalTranscript = "No transcript captured for this voice note.";
            }

            String audioData = null;
            if (file != null && !file.isEmpty()) {
                String contentType = file.getContentType() != null ? file.getContentType() : "audio/webm";
                audioData = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
            }

            // Build entity using Lombok Builder
            VoiceNote note = VoiceNote.builder()
                    .title(title)
                    .tag(tagEnum)
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .transcript(finalTranscript)
                    .audioUrl(audioData)
                    .build();

            voiceNoteRepository.save(note);
            return ResponseEntity.ok(Map.of("message", "Voice note saved successfully"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal Server Error"));
        }
    }

    @GetMapping
    public ResponseEntity<List<VoiceNote>> getUserVoiceNotes() {
        User user = getAuthenticatedUser();
        return ResponseEntity.ok(voiceNoteRepository.findByUser(user));
    }

    /**
     * Uploads the audio byte array to AssemblyAI and polls for transcription completion.
     */
    private String transcribeWithAssemblyAI(byte[] audioBytes) {
        try {
            String cleanKey = assemblyApiKey.trim();
            System.out.println(">>> [AssemblyAI] Initializing upload (Data size: " + audioBytes.length + " bytes)...");

            RestTemplate restTemplate = new RestTemplate();

            // Step 1: Upload raw binary audio
            HttpHeaders uploadHeaders = new HttpHeaders();
            uploadHeaders.set("authorization", cleanKey);
            uploadHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> uploadEntity = new HttpEntity<>(audioBytes, uploadHeaders);
            ResponseEntity<Map> uploadResponse = restTemplate.postForEntity(
                    "https://api.assemblyai.com/v2/upload",
                    uploadEntity,
                    Map.class
            );

            if (!uploadResponse.getStatusCode().is2xxSuccessful() || uploadResponse.getBody() == null) {
                System.err.println(">>> [AssemblyAI Error] Upload failed. HTTP " + uploadResponse.getStatusCode());
                return "";
            }

            String uploadUrl = (String) uploadResponse.getBody().get("upload_url");
            System.out.println(">>> [AssemblyAI] File uploaded. Temporary URL: " + uploadUrl);

            // Step 2: Queue transcription job
            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.set("authorization", cleanKey);
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = Map.of("audio_url", uploadUrl);
            HttpEntity<Map<String, String>> jobEntity = new HttpEntity<>(requestBody, jsonHeaders);

            ResponseEntity<Map> jobResponse = restTemplate.postForEntity(
                    "https://api.assemblyai.com/v2/transcript",
                    jobEntity,
                    Map.class
            );

            if (!jobResponse.getStatusCode().is2xxSuccessful() || jobResponse.getBody() == null) {
                System.err.println(">>> [AssemblyAI Error] Failed to queue transcription job.");
                return "";
            }

            String transcriptId = (String) jobResponse.getBody().get("id");
            String pollUrl = "https://api.assemblyai.com/v2/transcript/" + transcriptId;
            System.out.println(">>> [AssemblyAI] Job queued. Transcript ID: " + transcriptId);

            // Step 3: Poll status (up to 20 seconds)
            HttpEntity<Void> pollEntity = new HttpEntity<>(jsonHeaders);
            for (int i = 1; i <= 20; i++) {
                Thread.sleep(1000);

                ResponseEntity<Map> pollResponse = restTemplate.exchange(
                        pollUrl,
                        HttpMethod.GET,
                        pollEntity,
                        Map.class
                );

                if (pollResponse.getStatusCode().is2xxSuccessful() && pollResponse.getBody() != null) {
                    String status = (String) pollResponse.getBody().get("status");
                    System.out.println(">>> [AssemblyAI] Polling (" + i + "/20) - Status: " + status);

                    if ("completed".equals(status)) {
                        String text = (String) pollResponse.getBody().get("text");
                        System.out.println(">>> [AssemblyAI Success] Generated transcript: " + text);
                        return text;
                    } else if ("error".equals(status)) {
                        System.err.println(">>> [AssemblyAI Error] Engine error: " + pollResponse.getBody().get("error"));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(">>> [AssemblyAI Exception] " + e.getMessage());
            e.printStackTrace();
        }
        return "";
    }
}