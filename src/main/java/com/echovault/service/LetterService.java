package com.echovault.service;

import com.echovault.dto.LetterRequestDto;
import com.echovault.model.Letter;
import com.echovault.model.Tag;
import com.echovault.model.User;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LetterService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;

    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UsernameNotFoundException("Unauthorized access. Please log in.");
        }
        String currentEmail = auth.getName();
        return userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentEmail));
    }

    /**
     * DTO-based save method for Controller binding.
     */
    @Transactional
    public Letter saveLetter(LetterRequestDto dto) {
        String mainContent = dto.getBodyContent() != null && !dto.getBodyContent().isBlank()
                ? dto.getBodyContent()
                : dto.getContent();

        String mainTitle = dto.getTitle() != null && !dto.getTitle().isBlank()
                ? dto.getTitle()
                : dto.getSubject();

        return saveLetter(
                mainTitle,
                mainContent,
                dto.getRecipientName(),
                dto.getRecipientEmail(),
                dto.getTag(),
                dto.getIsPublic(),
                dto.getScheduledDeliveryAt()
        );
    }

    @Transactional
    public Letter saveLetter(String title, String content, String recipientName, String recipientEmail,
                             String tagStr, Boolean isPublic, LocalDateTime scheduledDeliveryAt) {

        User user = getAuthenticatedUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryTime = scheduledDeliveryAt != null ? scheduledDeliveryAt : now;

        // Letters MUST start as false so SendGrid/Scheduler picks them up.
        boolean isDelivered = false;

        // Safely extract public visibility flag
        boolean publicStatus = Boolean.TRUE.equals(isPublic);

        String displayTitle = (title == null || title.trim().isEmpty()) ? "Untitled Note" : title.trim();

        Tag parsedTag = Tag.MOTIVATIONAL;
        if (tagStr != null && !tagStr.isBlank()) {
            try {
                parsedTag = Tag.valueOf(tagStr.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                parsedTag = Tag.MOTIVATIONAL;
            }
        }

        Letter letter = Letter.builder()
                .title(displayTitle)
                .subject(displayTitle)
                .content(content)
                .bodyContent(content)
                .recipientName(recipientName)
                .recipientEmail(recipientEmail)
                .tag(parsedTag)
                .isPublic(publicStatus)
                .isDelivered(isDelivered)
                .scheduledDeliveryAt(deliveryTime)
                .createdAt(now)
                .user(user)
                .build();

        return letterRepository.save(letter);
    }

    @Transactional(readOnly = true)
    public List<Letter> getUserVaultLetters() {
        User user = getAuthenticatedUser();
        return letterRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<Letter> getMemorialLetters() {
        return letterRepository.findByIsPublicTrueAndIsDeliveredTrueOrderByCreatedAtDesc();
    }
}