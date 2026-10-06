package com.example.backend.profile;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    @EntityGraph(attributePaths = {"skills", "interests", "assessmentScores"})
    Optional<UserProfile> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}