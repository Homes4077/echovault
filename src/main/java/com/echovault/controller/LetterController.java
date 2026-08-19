package com.echovault.controller;

import com.echovault.model.Letter;
import com.echovault.service.LetterService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class LetterController {

    private final LetterService letterService;

    @Data
    public static class LetterRequest {
        private String recipientName;
        private String recipientEmail;
        private String subject;
        private String tag;
        private String scheduledDeliveryAt;
        private String bodyContent;
        private boolean isPublic;
    }

    @PostMapping("/vault/letter/compose")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createLetter(@RequestBody LetterRequest request) {
        try {
            LocalDateTime scheduledDate = parseDateTime(request.getScheduledDeliveryAt());

            Letter savedLetter = letterService.saveLetter(
                    request.getSubject(),
                    request.getBodyContent(),
                    request.getRecipientName(),
                    request.getRecipientEmail(),
                    request.getTag(),
                    request.isPublic(),
                    scheduledDate
            );

            log.info("Successfully created letter ID: {}", savedLetter.getId());
            return ResponseEntity.ok(Map.of("message", "Letter saved successfully", "id", savedLetter.getId()));

        } catch (UsernameNotFoundException e) {
            log.warn("Unauthorized attempt to compose letter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input when scheduling letter: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to compose letter: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to schedule letter: " + e.getMessage()));
        }
    }

    @GetMapping("/api/letters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getVaultLetters() {
        try {
            return ResponseEntity.ok(letterService.getUserVaultLetters());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/letters/memorial")
    public ResponseEntity<List<Letter>> getMemorialLetters() {
        return ResponseEntity.ok(letterService.getMemorialLetters());
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }

        String raw = dateStr.trim();
        String isoFormatted = raw.replace(" ", "T");

        try {
            return LocalDateTime.parse(isoFormatted);
        } catch (DateTimeParseException ignored) {
            // Fall back to alternative patterns
        }

        DateTimeFormatter[] fallbackFormatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        };

        for (DateTimeFormatter formatter : fallbackFormatters) {
            try {
                return LocalDateTime.parse(raw, formatter);
            } catch (DateTimeParseException ignored) {
                // Keep trying
            }
        }

        throw new IllegalArgumentException("Unsupported date format: '" + dateStr + "'. Use ISO format (YYYY-MM-DDTHH:mm).");
    }
}
