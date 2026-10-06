package com.example.backend.resume;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class ResumeDtos {
    private ResumeDtos() {}

    public record SaveRequest(
            @NotBlank @Size(max = 160) String name,
            @Size(max = 120) String targetRole,
            @NotBlank @Size(max = 30000) String content) {}

    public record Response(Long id, String name, String targetRole, String content,
                           Instant createdAt, Instant updatedAt) {}
}