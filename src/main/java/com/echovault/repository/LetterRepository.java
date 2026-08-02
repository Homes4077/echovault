package com.echovault.repository;

import com.echovault.model.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface LetterRepository extends JpaRepository<Letter, Long> {
    List<Letter> findByUserId(Long userId);

    @Query("SELECT l FROM Letter l WHERE l.scheduledDeliveryAt <= :now AND l.isDelivered = false")
    List<Letter> findPendingLettersToDeliver(LocalDateTime now);
}
