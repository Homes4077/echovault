package com.echovault.service;

import com.echovault.model.Photograph;
import com.echovault.model.User;
import com.echovault.repository.PhotographRepository;
import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhotographService {

    private final PhotographRepository photographRepository;
    private final UserRepository userRepository;

    private static final String UPLOAD_DIR = "uploads/";

    private User getOrCreateUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseGet(() -> userRepository.findAll().stream().findFirst()
                        .orElseGet(() -> {
                            User defaultUser = new User();
                            defaultUser.setEmail("testuser@echovault.com");
                            defaultUser.setPassword("password123");
                            return userRepository.save(defaultUser);
                        }));
    }

    public Photograph uploadAndSave(MultipartFile file, String title, String category, String caption, String userEmail) throws IOException {
        User user = getOrCreateUser(userEmail);

        File uploadFolder = new File(UPLOAD_DIR);
        if (!uploadFolder.exists()) {
            uploadFolder.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fileUrl = "/uploads/" + fileName;

        Photograph photo = Photograph.builder()
                .title(title != null && !title.isBlank() ? title : "Untitled Memory")
                .category(category != null && !category.isBlank() ? category.toUpperCase() : "GENERAL")
                .caption(caption != null && !caption.isBlank() ? caption : "")
                .imageUrl(fileUrl)
                .createdAt(LocalDateTime.now())
                .user(user)
                .build();

        return photographRepository.save(photo);
    }

    public List<Photograph> getPhotosForUser(String userEmail) {
        User user = getOrCreateUser(userEmail);
        return photographRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Photograph> getAllPublicPhotos() {
        return photographRepository.findAllByOrderByCreatedAtDesc();
    }
}