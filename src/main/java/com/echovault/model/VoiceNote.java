package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String cloudinaryUrl;

    @Column(nullable = false)
    private String cloudinaryPublicId;

    @Column(columnDefinition = "LONGTEXT")
    private String transcription;

    @Enumerated(EnumType.STRING)
    private TranscriptionStatus transcriptionStatus = TranscriptionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tag tag;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TranscriptionStatus { PENDING, COMPLETED, FAILED }
    public enum Tag { MOTIVATIONAL, LOVE, STORY, WARNING, FAITH, CELEBRATION }
}
