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
    private String audioUrl;

    @Enumerated(EnumType.STRING)
    private Tag tag;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt;
}
