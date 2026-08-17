package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    
    private String email;
    private String username;
    private String password;

    // Fallback getter if frontend submits 'username' instead of 'email'
    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }
}
