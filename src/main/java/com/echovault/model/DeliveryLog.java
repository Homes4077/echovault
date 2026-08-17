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

    public enum DeliveryType {
        LETTER,
        VOICE_NOTE,
        PHOTOGRAPH,
        EMAIL,
        SYSTEM
    }

    public enum Status {
        SUCCESS,
        FAILED,
        PENDING,
        DELIVERED,
        SENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String recipient;
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String triggerReason;
    private String details;
    private LocalDateTime timestamp;
}
