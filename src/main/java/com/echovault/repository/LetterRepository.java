package com.echovault.repository;

import com.echovault.model.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    List<Letter> findByUserId(Long userId);
    
    List<Letter> findByIsDeliveredFalseAndScheduledDeliveryAtBefore(LocalDateTime now);

    @Query("SELECT l FROM Letter l WHERE l.isDelivered = false AND l.scheduledDeliveryAt <= :now")
    List<Letter> findPendingLettersToDeliver(@Param("now") LocalDateTime now);
}
