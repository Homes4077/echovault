package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"password", "profilePicture", "securityAnswer"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true)
    private String username;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String profilePicture;

    @Builder.Default
    private String role = "ROLE_USER";

    private LocalDateTime lastLoginAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Emergency protocol fields
    private String securityQuestion;
    private String securityAnswer;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Explicit Getters to ensure Docker/Maven compiler compatibility
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    // Helper method to maintain backwards compatibility with getName() calls
    public String getName() {
        return this.fullName;
    }

    public void setName(String name) {
        this.fullName = name;
    }

    // Explicit alias getters to ensure compilation compatibility
    public String getEmergencyQuestion() {
        return this.securityQuestion;
    }

    public String getEmergencyAnswer() {
        return this.securityAnswer;
    }

    // Custom getter to access the display username explicitly
    public String getDisplayUsername() {
        return this.username;
    }

    // Custom setter for display username
    public void setDisplayUsername(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String assignedRole = (role != null && !role.isBlank()) ? role : "ROLE_USER";
        if (!assignedRole.startsWith("ROLE_")) {
            assignedRole = "ROLE_" + assignedRole;
        }
        return List.of(new SimpleGrantedAuthority(assignedRole));
    }

    @Override
    public String getUsername() {
        return email; // Retained for Spring Security JWT principal resolution
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
