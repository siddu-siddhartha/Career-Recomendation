package com.example.backend.roadmap;

import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class RoadmapDtos {
    private RoadmapDtos() {}

    public record Challenge(String prompt, List<String> options) {}

    public record Item(Long id, Long roleId, String skill, String title, String nextStep,
                       boolean completed, Instant createdAt, Instant completedAt, Challenge challenge) {}

    public record CompletionRequest(@NotBlank @Size(max = 1) String answer) {}
}