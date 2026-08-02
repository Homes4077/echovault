package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_ACCOUNT_OWNER;

    private LocalDateTime lastLoginAt = LocalDateTime.now();
    private Integer inactivityThresholdDays = 30;
    private Boolean inactivityAlertSent = false;

    private String securityQuestion;
    private String securityAnswerHash;
    private Integer failedUnlockAttempts = 0;
    private LocalDateTime lockedUntil;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role {
        ROLE_ADMIN, ROLE_ACCOUNT_OWNER, ROLE_FAMILY_MEMBER
    }
}
