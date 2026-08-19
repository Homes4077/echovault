package com.echovault.service;

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

    public Letter saveLetter(String title, String content, String recipientName, String recipientEmail,
                             String tagStr, boolean isPublic, LocalDateTime scheduledDeliveryAt) {
        
        User user = getAuthenticatedUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryTime = scheduledDeliveryAt != null ? scheduledDeliveryAt : now;

        // FIXED: Letters MUST start as false so SendGrid/Scheduler picks them up.
        // Never auto-mark as true here, even if scheduled for right now.
        boolean isDelivered = false;

        String displayTitle = (title == null || title.trim().isEmpty()) ? "Untitled Note" : title;

        Tag parsedTag = Tag.MOTIVATIONAL;
        if (tagStr != null && !tagStr.isBlank()) {
            try {
                parsedTag = Tag.valueOf(tagStr.toUpperCase());
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
                .isPublic(isPublic)
                .isDelivered(isDelivered)
                .scheduledDeliveryAt(deliveryTime)
                .createdAt(now)
                .user(user)
                .build();

        return letterRepository.save(letter);
    }

    public List<Letter> getUserVaultLetters() {
        User user = getAuthenticatedUser();
        // FIXED: Pure read query. Removed side-effect loop that was prematurely flipping isDelivered to true.
        return letterRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<Letter> getMemorialLetters() {
        // FIXED: Pure read query. Removed side-effect mutation during GET request.
        return letterRepository.findByIsPublicTrueAndIsDeliveredTrueOrderByCreatedAtDesc();
    }
}
