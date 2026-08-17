package com.echovault.repository;

import com.echovault.model.Letter;
import com.echovault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LetterRepository extends JpaRepository<Letter, Long> {

    List<Letter> findByUser(User user);

    List<Letter> findByUserOrderByCreatedAtDesc(User user);

    List<Letter> findByIsPublicTrueOrderByCreatedAtDesc();

    List<Letter> findAllByIsDeliveredFalseAndScheduledDeliveryAtBefore(LocalDateTime dateTime);
}
