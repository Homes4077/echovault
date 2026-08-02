package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ghost_triggers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhostTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerType triggerType;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;

    @Column(nullable = false)
    private LocalDateTime nextRunAt;

    private Boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TriggerType { ANNIVERSARY, BIRTHDAY, INACTIVITY_CHECK, SCHEDULED_LETTER }
}
