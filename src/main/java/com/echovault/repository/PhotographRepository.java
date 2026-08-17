package com.echovault.repository;

import com.echovault.model.Photograph;
import com.echovault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotographRepository extends JpaRepository<Photograph, Long> {

    List<Photograph> findByUser(User user);

    List<Photograph> findByUserOrderByCreatedAtDesc(User user);

    List<Photograph> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    List<Photograph> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Photograph> findAllByOrderByCreatedAtDesc();
}
