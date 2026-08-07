package com.echovault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "letters")
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String recipientName;
    private String recipientEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String bodyContent;

    private String tag;
    private LocalDateTime scheduledDeliveryAt;

    @Builder.Default
    private Boolean isDelivered = false;

    private LocalDateTime deliveredAt;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
