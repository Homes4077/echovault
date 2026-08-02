package com.echovault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ghost_queries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhostQuery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_owner_id", nullable = false)
    private User vaultOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queried_by_id", nullable = false)
    private User queriedBy;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String queryText;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String responseText;

    @Column(columnDefinition = "TEXT")
    private String sourcesUsed;

    private LocalDateTime queriedAt = LocalDateTime.now();
}
