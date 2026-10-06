package com.example.backend.application;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "career_applications")
public class CareerApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 120)
    private String role;

    @Column(nullable = false, length = 120)
    private String company;

    @Column(length = 2048)
    private String postingUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ApplicationStatus status;

    private LocalDate appliedDate;

    @Column(length = 1500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected CareerApplication() {}

    public CareerApplication(String email, String role, String company, String postingUrl,
                             ApplicationStatus status, LocalDate appliedDate, String notes) {
        this.email = email;
        this.role = role;
        this.company = company;
        this.postingUrl = postingUrl;
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getCompany() { return company; }
    public String getPostingUrl() { return postingUrl; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDate getAppliedDate() { return appliedDate; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }

    public void update(ApplicationStatus status, LocalDate appliedDate, String notes) {
        this.status = status;
        this.appliedDate = appliedDate;
        this.notes = notes;
    }
}