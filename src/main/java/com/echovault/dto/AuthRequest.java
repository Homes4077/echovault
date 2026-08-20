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
    private String viewMode; // "USER", "FAMILY", or "ADMIN"
  private String securityAnswer;

    // Fallback getter if frontend submits 'username' instead of 'email'
    public String getEmail() {
        if (email != null && !email.isBlank()) {
            return email;
        }
        return username;
    }

    // Fallback getter to guarantee a default mode if omitted
    public String getViewMode() {
        if (viewMode != null && !viewMode.isBlank()) {
            return viewMode;
        }
        return "USER";
    }
}
