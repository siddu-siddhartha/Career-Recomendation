package com.example.backend.application;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class ApplicationDtos {
    private ApplicationDtos() {}

    public record CreateRequest(
            @NotBlank @Size(max = 120) String role,
            @NotBlank @Size(max = 120) String company,
            @Size(max = 2048) String postingUrl,
            LocalDate appliedDate,
            @Size(max = 1500) String notes) {}

    public record UpdateRequest(
            @NotNull ApplicationStatus status,
            LocalDate appliedDate,
            @Size(max = 1500) String notes) {}

    public record Response(Long id, String role, String company, String postingUrl,
                           ApplicationStatus status, LocalDate appliedDate, String notes,
                           Instant createdAt) {}
}