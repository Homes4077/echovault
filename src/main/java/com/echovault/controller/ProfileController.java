package com.echovault.controller;

import com.echovault.dto.ProfileUpdateRequest;
import com.echovault.model.User;
import com.echovault.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("User not found."));
        }

        return ResponseEntity.ok(buildProfileResponse(user, null));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication,
                                           @RequestBody ProfileUpdateRequest request) {
        User user = getCurrentUser(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("User not found."));
        }

        if (request == null || request.getUsername() == null || !request.getUsername().matches("^[a-zA-Z0-9_]{3,20}$")) {
            return ResponseEntity.badRequest()
                    .body(errorBody("Username must be 3-20 characters: letters, numbers, underscores."));
        }

        String newUsername = request.getUsername().trim();

        // Check if the new username is already taken by a different user
        Optional<User> existingUser = userRepository.findByUsername(newUsername);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(errorBody("Username is already taken. Please choose another."));
        }

        user.setUsername(newUsername);

        if (request.getProfilePicture() != null) {
            user.setProfilePicture(request.getProfilePicture());
        }

        userRepository.save(user);

        return ResponseEntity.ok(buildProfileResponse(user, "Profile updated successfully."));
    }

    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private Map<String, Object> buildProfileResponse(User user, String successMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("email", user.getEmail());
        response.put("fullName", user.getFullName());
        response.put("username", user.getDisplayUsername());
        response.put("profilePicture", user.getProfilePicture());
        response.put("role", user.getRole());

        if (successMessage != null) {
            response.put("message", successMessage);
        }
        return response;
    }

    private Map<String, Object> errorBody(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        return body;
    }
}