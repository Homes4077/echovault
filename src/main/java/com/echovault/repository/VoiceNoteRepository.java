package com.echovault.repository;

import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoiceNoteRepository extends JpaRepository<VoiceNote, Long> {
    List<VoiceNote> findByUser(User user);
    List<VoiceNote> findByUserId(Long userId);
}
