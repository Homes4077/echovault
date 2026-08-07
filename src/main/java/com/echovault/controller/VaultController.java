package com.echovault.controller;

import com.echovault.dto.LetterRequestDto;
import com.echovault.dto.VoiceUploadDto;
import com.echovault.model.Letter;
import com.echovault.model.VoiceNote;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import com.echovault.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final LetterRepository letterRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public VaultController(LetterRepository letterRepository, 
                           VoiceNoteRepository voiceNoteRepository, 
                           UserRepository userRepository, 
                           CloudinaryService cloudinaryService) {
        this.letterRepository = letterRepository;
        this.voiceNoteRepository = voiceNoteRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/letters")
    public ResponseEntity<?> createLetter(@RequestBody LetterRequestDto dto) {
        return userRepository.findById(dto.getUserId())
            .map(user -> {
                Letter letter = Letter.builder()
                    .user(user)
                    .recipientName(dto.getRecipientName())
                    .recipientEmail(dto.getRecipientEmail())
                    .subject(dto.getSubject())
                    .bodyContent(dto.getBodyContent())
                    .tag(dto.getTag())
                    .scheduledDeliveryAt(dto.getScheduledDeliveryAt())
                    .build();
                return ResponseEntity.ok(letterRepository.save(letter));
            })
            .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/letters/user/{userId}")
    public ResponseEntity<List<Letter>> getUserLetters(@PathVariable Long userId) {
        return ResponseEntity.ok(letterRepository.findByUserId(userId));
    }

    @PostMapping("/voice-notes/upload")
    public ResponseEntity<?> uploadVoiceNote(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") VoiceUploadDto dto) throws IOException {

        Map uploadResult = cloudinaryService.uploadFile(file);
        
        return userRepository.findById(dto.getUserId())
            .map(user -> {
                VoiceNote voiceNote = VoiceNote.builder()
                    .user(user)
                    .title(dto.getTitle())
                    .tag(dto.getTag())
                    .cloudinaryUrl(uploadResult.get("secure_url").toString())
                    .cloudinaryPublicId(uploadResult.get("public_id").toString())
                    .transcriptionStatus(VoiceNote.TranscriptionStatus.PENDING)
                    .build();
                return ResponseEntity.ok(voiceNoteRepository.save(voiceNote));
            })
            .orElse(ResponseEntity.badRequest().build());
    }
}
