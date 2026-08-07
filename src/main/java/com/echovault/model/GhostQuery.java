package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ghost_queries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GhostQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_owner_id")
    private User vaultOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queried_by_id")
    private User queriedBy;

    @Column(columnDefinition = "TEXT")
    private String queryText;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
