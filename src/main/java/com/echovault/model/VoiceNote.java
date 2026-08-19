package com.echovault.model;

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

    private String title;

    @Lob
    @Column(name = "audio_url", columnDefinition = "LONGTEXT")
    private String audioUrl;

    @Lob
    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
