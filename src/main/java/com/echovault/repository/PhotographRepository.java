package com.echovault.repository;

import com.echovault.model.Photograph;
import com.echovault.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotographRepository extends JpaRepository<Photograph, Long> {
    List<Photograph> findByUserId(Long userId);
    List<Photograph> findByTag(Tag tag);
}
