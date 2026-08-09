package com.echovault.repository;

import com.echovault.model.Letter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {
    List<Letter> findAllByIsDeliveredFalseAndScheduledDeliveryAtBefore(LocalDateTime time);
}
