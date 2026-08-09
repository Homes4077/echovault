package com.echovault.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "user_id")
    private User user;

    private String recipientName;
    private String recipientEmail;
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String bodyContent;

    private String tag;
    private LocalDateTime scheduledDeliveryAt;
    private boolean isDelivered;
    private LocalDateTime deliveredAt;
}
