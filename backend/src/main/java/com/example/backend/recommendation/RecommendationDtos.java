package com.example.backend.recommendation;

import java.util.List;

public final class RecommendationDtos {
    private RecommendationDtos() {}

    public record Match(Long roleId, String role, String description, String industry, double matchScore,
                        List<String> matchedSkills, List<String> missingSkills, double skillFit,
                        double interestFit, double educationFit, double quizFit, double academicFit) {}
    public record Gap(String skill, String status, int priority, String suggestion) {}
    public record RoadmapItem(String skill, String title, String resourceType, String nextStep) {}
    public record RoleDetail(Match match, List<Gap> gaps, List<RoadmapItem> roadmap) {}
    public record Snapshot(Long id, Long roleId, double matchScore, String generatedAt) {}
}