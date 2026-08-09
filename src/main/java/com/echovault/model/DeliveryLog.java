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
@Table(name = "delivery_logs")
public class DeliveryLog {

    public enum DeliveryType {
        EMAIL, SMS, EMERGENCY_TRIGGER
    }

    public enum Status {
        SENT, FAILED, PENDING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private DeliveryType deliveryType;

    private String recipient;
    private String triggerReason;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime timestamp;
}
