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
    private DeliveryType deliveryType;

    private String recipient;
    private String triggerReason;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum DeliveryType {
        EMAIL, SMS, SENDGRID_EMAIL, TWILIO_SMS
    }

    public enum Status {
        SENT, FAILED, SUCCESS
    }
}
