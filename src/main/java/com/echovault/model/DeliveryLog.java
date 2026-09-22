package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", columnDefinition = "VARCHAR(255)")
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "VARCHAR(255)")
    private Status status;

    private String recipient;
    private String recipientEmail;
    private String details;
    private String triggerReason;
    private LocalDateTime timestamp;

    public enum DeliveryType {
        EMAIL,
        LETTER,
        PHOTOGRAPH,
        SYSTEM,
        VOICE_NOTE
    }

    public enum Status {
        DELIVERED,
        FAILED,
        PENDING,
        SENT,
        SUCCESS
    }
}