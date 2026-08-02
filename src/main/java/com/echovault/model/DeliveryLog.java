package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryType deliveryType;

    @Column(nullable = false)
    private String recipient;

    @Column(nullable = false)
    private String triggerReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime deliveredAt = LocalDateTime.now();

    public enum DeliveryType { SENDGRID_EMAIL, TWILIO_SMS }
    public enum Status { SUCCESS, FAILED, DEMO_TRIGGERED }
}
