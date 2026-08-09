package com.echovault.controller;

import com.echovault.dto.LetterRequestDto;
import com.echovault.dto.VoiceUploadDto;
import com.echovault.model.Letter;
import com.echovault.model.Tag;
import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/vault")
@RequiredArgsConstructor
public class VaultController {

    private final UserRepository userRepository;
    private final LetterRepository letterRepository;
    private final VoiceNoteRepository voiceNoteRepository;

    @PostMapping("/letter")
    public ResponseEntity<Letter> createLetter(@RequestBody LetterRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tag parsedTag = parseTag(dto.getTag());

        Letter letter = Letter.builder()
                .user(user)
                .recipientName(dto.getRecipientName())
                .recipientEmail(dto.getRecipientEmail())
                .subject(dto.getSubject())
                .bodyContent(dto.getBodyContent())
                .tag(parsedTag.name())
                .scheduledDeliveryAt(dto.getScheduledDeliveryAt())
                .isDelivered(false)
                .build();

        return ResponseEntity.ok(letterRepository.save(letter));
    }

    @PostMapping("/voice")
    public ResponseEntity<VoiceNote> uploadVoiceNote(@RequestBody VoiceUploadDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Tag parsedTag = parseTag(dto.getTag());

        VoiceNote voiceNote = VoiceNote.builder()
                .user(user)
                .title(dto.getTitle())
                .tag(parsedTag)
                .createdAt(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(voiceNoteRepository.save(voiceNote));
    }

    private Tag parseTag(String tagStr) {
        if (tagStr == null || tagStr.trim().isEmpty()) {
            return Tag.OTHER;
        }
        try {
            return Tag.valueOf(tagStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Tag.OTHER;
        }
    }
}
