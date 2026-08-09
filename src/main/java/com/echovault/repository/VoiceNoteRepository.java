package com.echovault.repository;

import com.echovault.model.VoiceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoiceNoteRepository extends JpaRepository<VoiceNote, Long> {
}
