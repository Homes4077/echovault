package com.echovault.repository;

import com.echovault.model.PersonalityProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PersonalityProfileRepository extends JpaRepository<PersonalityProfile, Long> {
    Optional<PersonalityProfile> findByUserId(Long userId);
}
