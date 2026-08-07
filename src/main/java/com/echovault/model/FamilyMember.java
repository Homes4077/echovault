package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String phoneNumber;
    private String relationship;

    @Enumerated(EnumType.STRING)
    private PermissionLevel permissionLevel;

    public enum PermissionLevel {
        VIEW,
        EDIT,
        ADMIN,
        EMERGENCY_ONLY,
        EMERGENCY_CONTACT
    }
}
