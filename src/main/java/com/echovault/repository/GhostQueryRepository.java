package com.echovault.repository;

import com.echovault.model.GhostQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GhostQueryRepository extends JpaRepository<GhostQuery, Long> {
    List<GhostQuery> findByVaultOwnerId(Long vaultOwnerId);
}
