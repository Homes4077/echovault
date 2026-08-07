package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "letters")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    private Boolean isDelivered = false;

    private LocalDateTime deliveredAt;
}
