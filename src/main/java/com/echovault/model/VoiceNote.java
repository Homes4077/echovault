package com.echovault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "voice_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    private String title;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    private String cloudinaryUrl;
    private String cloudinaryPublicId;

    @Column(columnDefinition = "TEXT")
    private String transcription;

    @Enumerated(EnumType.STRING)
    private TranscriptionStatus transcriptionStatus;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Tag {
        MEMORY, ADVICE, LEGAL, PERSONAL, GREETING
    }

    public enum TranscriptionStatus {
        PENDING, COMPLETED, FAILED
    }
}
