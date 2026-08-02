package com.echovault.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ghost_queries")
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

    @Column(columnDefinition = "TEXT")
    private String responseText;

    private String sourcesUsed;

    private LocalDateTime queriedAt;

    @PrePersist
    protected void onCreate() {
        this.queriedAt = LocalDateTime.now();
    }
}
