package com.echovault.controller;

import com.echovault.model.Photograph;
import com.echovault.service.PhotographService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhotographController {

    private final PhotographService photographService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(value = "category", defaultValue = "GENERAL") String category,
            @RequestParam(value = "caption", defaultValue = "") String caption,
            Principal principal) {

        // Fallback email for testing when SecurityConfig permitsAll or token is missing
        String userEmail;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            userEmail = principal.getName();
        } else {
            log.warn("Principal is null on upload. Using fallback test user identity.");
            userEmail = "testuser@echovault.com"; // Change to an existing user email in your DB if needed
        }

        try {
            List<Photograph> savedPhotos = new ArrayList<>();

            if (files != null && !files.isEmpty()) {
                for (MultipartFile f : files) {
                    if (!f.isEmpty()) {
                        savedPhotos.add(photographService.uploadAndSave(f, title, category, caption, userEmail));
                    }
                }
            } else if (file != null && !file.isEmpty()) {
                savedPhotos.add(photographService.uploadAndSave(file, title, category, caption, userEmail));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "No file selected."));
            }

            return ResponseEntity.ok(savedPhotos);

        } catch (Exception e) {
            log.error("Error uploading photo for user: {}", userEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserPhotos(Principal principal) {
        String userEmail;
        if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
            userEmail = principal.getName();
        } else {
            log.warn("Principal is null on fetch. Using fallback test user identity.");
            userEmail = "testuser@echovault.com";
        }

        List<Photograph> photos = photographService.getPhotosForUser(userEmail);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Photograph>> getPublicMemorialPhotos() {
        List<Photograph> photos = photographService.getAllPublicPhotos();
        return ResponseEntity.ok(photos);
    }
}
