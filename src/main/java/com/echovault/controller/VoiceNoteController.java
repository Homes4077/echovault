package com.echovault.controller;

import com.echovault.model.Tag;
import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/voice-notes")
public class VoiceNoteController {

    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;

    public VoiceNoteController(VoiceNoteRepository voiceNoteRepository, UserRepository userRepository) {
        this.voiceNoteRepository = voiceNoteRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> saveVoiceNote(
            @RequestParam("title") String title,
            @RequestParam(value = "tag", defaultValue = "MOTIVATIONAL") String tagStr,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        User user = userRepository.findByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tag tagEnum;
        try {
            tagEnum = Tag.valueOf(tagStr.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            tagEnum = Tag.MOTIVATIONAL;
        }

        VoiceNote note = new VoiceNote();
        note.setTitle(title);
        note.setTag(tagEnum);
        note.setUser(user);

        if (file != null && !file.isEmpty()) {
            String audioData = "data:" + file.getContentType() + ";base64," + Base64.getEncoder().encodeToString(file.getBytes());
            note.setAudioUrl(audioData);
        }

        voiceNoteRepository.save(note);
        return ResponseEntity.ok(Map.of("message", "Voice note saved successfully"));
    }

    @GetMapping
    public ResponseEntity<List<VoiceNote>> getUserVoiceNotes(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsernameOrEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(voiceNoteRepository.findByUser(user));
    }
}
