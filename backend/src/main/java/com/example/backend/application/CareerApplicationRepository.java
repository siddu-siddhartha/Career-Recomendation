package com.example.backend.application;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerApplicationRepository extends JpaRepository<CareerApplication, Long> {
    List<CareerApplication> findByEmailOrderByCreatedAtDesc(String email);
    Optional<CareerApplication> findByIdAndEmail(Long id, String email);
}