package com.echovault.repository;

import com.echovault.model.VoiceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VoiceNoteRepository extends JpaRepository<VoiceNote, Long> {
    List<VoiceNote> findByUserId(Long userId);
}
