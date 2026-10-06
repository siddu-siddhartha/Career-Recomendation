package com.example.backend.roadmap;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class RoadmapItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private Long roleId;
    private String skill;
    private String title;
    private String nextStep;
    private boolean completed;
    private Instant createdAt;
    private Instant completedAt;

    protected RoadmapItem() {}

    public RoadmapItem(String email, Long roleId, String skill, String title, String nextStep) {
        this.email = email;
        this.roleId = roleId;
        this.skill = skill;
        this.title = title;
        this.nextStep = nextStep;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public Long getRoleId() { return roleId; }
    public String getSkill() { return skill; }
    public String getTitle() { return title; }
    public String getNextStep() { return nextStep; }
    public boolean isCompleted() { return completed; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void complete() {
        if (!completed) {
            completed = true;
            completedAt = Instant.now();
        }
    }
}