package com.echovault.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "letters")
public class Letter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String subject;

    @Column(name = "body_content", columnDefinition = "TEXT")
    private String bodyContent;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Builder.Default
    @Column(name = "is_public", nullable = false, columnDefinition = "boolean default false")
    private boolean isPublic = false;

    @Builder.Default
    @Column(name = "is_delivered", nullable = false, columnDefinition = "boolean default false")
    private boolean isDelivered = false;

    @Column(name = "scheduled_delivery_at")
    private LocalDateTime scheduledDeliveryAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private Tag tag;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // ==========================================
    // BACKWARD COMPATIBILITY ALIASES
    // ==========================================

    public String getContent() {
        return this.bodyContent;
    }

    public void setContent(String content) {
        this.bodyContent = content;
    }

    public static class LetterBuilder {
        public LetterBuilder content(String content) {
            this.bodyContent = content;
            return this;
        }
    }
}
