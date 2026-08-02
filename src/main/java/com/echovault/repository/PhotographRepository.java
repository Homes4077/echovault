package com.echovault.repository;

import com.echovault.model.Photograph;
import com.echovault.model.VoiceNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotographRepository extends JpaRepository<Photograph, Long> {
    List<Photograph> findByUserId(Long userId);
    List<Photograph> findByUserIdAndTag(Long userId, VoiceNote.Tag tag);
}
