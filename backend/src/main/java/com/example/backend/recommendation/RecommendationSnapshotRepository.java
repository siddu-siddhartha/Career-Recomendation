package com.example.backend.recommendation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationSnapshotRepository extends JpaRepository<RecommendationSnapshot, Long> {
    List<RecommendationSnapshot> findTop20ByEmailOrderByGeneratedAtDesc(String email);
}