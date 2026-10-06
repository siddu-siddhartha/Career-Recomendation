package com.example.backend.profile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String passwordHash;
    private String education;
    private Integer experienceYears;
    private String workStyle;
    private String preferredIndustry;
    private Integer academicScore;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "profile_id"))
    private Set<UserSkill> skills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "profile_id"))
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_assessment_scores", joinColumns = @JoinColumn(name = "profile_id"))
    @MapKeyColumn(name = "category")
    @Column(name = "score")
    private Map<String, Integer> assessmentScores = new HashMap<>();

    protected UserProfile() {
    }

    public UserProfile(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getEducation() { return education; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getWorkStyle() { return workStyle; }
    public String getPreferredIndustry() { return preferredIndustry; }
    public Integer getAcademicScore() { return academicScore; }
    public Set<UserSkill> getSkills() { return skills; }
    public Set<String> getInterests() { return interests; }
    public Map<String, Integer> getAssessmentScores() { return assessmentScores; }

    public void updateProfile(String education, Integer experienceYears, String workStyle,
                              String preferredIndustry, Set<UserSkill> skills, Set<String> interests,
                              Integer academicScore, Map<String, Integer> assessmentScores) {
        this.education = education;
        this.experienceYears = experienceYears;
        this.workStyle = workStyle;
        this.preferredIndustry = preferredIndustry;
        this.skills = skills == null ? new HashSet<>() : new HashSet<>(skills);
        this.interests = interests == null ? new HashSet<>() : new HashSet<>(interests);
        this.academicScore = academicScore;
        this.assessmentScores = assessmentScores == null ? new HashMap<>() : new HashMap<>(assessmentScores);
    }
}