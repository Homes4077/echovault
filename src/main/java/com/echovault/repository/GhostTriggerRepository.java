package com.echovault.repository;

import com.echovault.model.GhostTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface GhostTriggerRepository extends JpaRepository<GhostTrigger, Long> {
    @Query("SELECT gt FROM GhostTrigger gt WHERE gt.nextRunAt <= :now AND gt.isActive = true")
    List<GhostTrigger> findDueTriggers(LocalDateTime now);
}
