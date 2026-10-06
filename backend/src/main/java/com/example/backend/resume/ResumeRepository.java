package com.example.backend.resume;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<ResumeDocument, Long> {
    List<ResumeDocument> findByEmailOrderByUpdatedAtDesc(String email);
    Optional<ResumeDocument> findByIdAndEmail(Long id, String email);
}