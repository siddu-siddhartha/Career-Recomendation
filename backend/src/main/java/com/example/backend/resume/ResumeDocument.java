package com.example.backend.resume;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_resumes")
public class ResumeDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 120)
    private String targetRole;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ResumeDocument() {}

    public ResumeDocument(String email, String name, String targetRole, String content) {
        this.email = email;
        this.name = name;
        this.targetRole = targetRole;
        this.content = content;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getTargetRole() { return targetRole; }
    public String getContent() { return content; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(String name, String targetRole, String content) {
        this.name = name;
        this.targetRole = targetRole;
        this.content = content;
        this.updatedAt = Instant.now();
    }
}