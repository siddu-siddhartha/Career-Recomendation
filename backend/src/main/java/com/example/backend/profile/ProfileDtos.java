package com.example.backend.profile;

import java.util.Map;
import java.util.Set;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public final class ProfileDtos {
    private ProfileDtos() {}

    public record SkillInput(String name, Proficiency proficiency) {}
    public record UpdateRequest(String education, @Min(0) Integer experienceYears, String workStyle,
                                  String preferredIndustry, @Valid Set<SkillInput> skills,
                                  Set<String> interests, @Min(0) @Max(100) Integer academicScore,
                                Map<String, @Min(0) @Max(100) Integer> assessmentScores) {}
    public record Response(Long id, String email, String education, Integer experienceYears,
                              String workStyle, String preferredIndustry, Set<SkillInput> skills,
                              Set<String> interests, Integer academicScore,
                              Map<String, Integer> assessmentScores) {}
}