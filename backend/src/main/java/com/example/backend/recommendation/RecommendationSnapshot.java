package com.example.backend.recommendation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class RecommendationSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Long roleId;
    private double matchScore;
    private Instant generatedAt;

    protected RecommendationSnapshot() {}

    public RecommendationSnapshot(String email, Long roleId, double matchScore) {
        this.email = email;
        this.roleId = roleId;
        this.matchScore = matchScore;
        this.generatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Long getRoleId() { return roleId; }
    public double getMatchScore() { return matchScore; }
    public Instant getGeneratedAt() { return generatedAt; }
}