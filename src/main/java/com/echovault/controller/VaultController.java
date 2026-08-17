package com.echovault.controller;

import com.echovault.model.Letter;
import com.echovault.model.Tag;
import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final LetterRepository letterRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;

    public VaultController(LetterRepository letterRepository, 
                           VoiceNoteRepository voiceNoteRepository, 
                           UserRepository userRepository) {
        this.letterRepository = letterRepository;
        this.voiceNoteRepository = voiceNoteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getVaultContents(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Letter> letters = letterRepository.findByUser(user);
        List<VoiceNote> voiceNotes = voiceNoteRepository.findByUser(user);

        Map<String, Object> response = new HashMap<>();
        response.put("letters", letters);
        response.put("voiceNotes", voiceNotes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/letter")
    public ResponseEntity<?> saveLetter(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userRepository.findByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String rawTag = request.getOrDefault("tag", "MOTIVATIONAL");
        Tag tagEnum;
        try {
            tagEnum = Tag.valueOf(rawTag.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            tagEnum = Tag.MOTIVATIONAL;
        }

        Letter letter = Letter.builder()
                .title(request.getOrDefault("title", "Untitled Letter"))
                .subject(request.get("subject"))
                .bodyContent(request.get("bodyContent"))
                .content(request.get("content"))
                .recipientName(request.get("recipientName"))
                .recipientEmail(request.get("recipientEmail"))
                .tag(tagEnum)
                .user(user)
                .build();

        letterRepository.save(letter);
        return ResponseEntity.ok(Map.of("message", "Letter saved to vault successfully"));
    }
}
