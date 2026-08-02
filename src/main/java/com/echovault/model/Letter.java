package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "letters")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String recipientEmail;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String bodyContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoiceNote.Tag tag;

    @Column(nullable = false)
    private LocalDateTime scheduledDeliveryAt;

    private Boolean isDelivered = false;
    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt = LocalDateTime.now();
}
