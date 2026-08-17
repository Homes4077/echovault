package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    private String name;
    private String fullName;
    private String email;
    private String password;
    private String role; // "ROLE_USER" or "ROLE_ADMIN"

    // Helper getter to resolve whether 'name' or 'fullName' was sent in the request
    public String getFullName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return name;
    }

    // Helper getter to ensure compatibility with User entity setters
    public String getName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return fullName;
    }
}
