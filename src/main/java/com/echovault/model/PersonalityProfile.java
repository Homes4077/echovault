package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "personality_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalityProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(columnDefinition = "TEXT")
    private String coreValues;

    @Column(columnDefinition = "TEXT")
    private String favoritePhrases;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    private LocalDateTime updatedAt = LocalDateTime.now();
}
