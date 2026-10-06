package com.example.backend.role;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "career_roles")
public class CareerRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String minEducation;
    private String industry;

    @ElementCollection
    @CollectionTable(name = "role_required_skills", joinColumns = @JoinColumn(name = "role_id"))
    private Set<RequiredSkill> requiredSkills = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "role_interests", joinColumns = @JoinColumn(name = "role_id"))
    private Set<String> interests = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "role_assessment_categories", joinColumns = @JoinColumn(name = "role_id"))
    private Set<String> assessmentCategories = new HashSet<>();

    protected CareerRole() {
    }

    public CareerRole(String name, String description, String minEducation, String industry,
                      Set<RequiredSkill> requiredSkills, Set<String> interests) {
        this.name = name;
        this.description = description;
        this.minEducation = minEducation;
        this.industry = industry;
        this.requiredSkills = requiredSkills;
        this.interests = interests;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getMinEducation() { return minEducation; }
    public String getIndustry() { return industry; }
    public Set<RequiredSkill> getRequiredSkills() { return requiredSkills; }
    public Set<String> getInterests() { return interests; }
    public Set<String> getAssessmentCategories() { return assessmentCategories; }
    public void setAssessmentCategories(Set<String> assessmentCategories) {
        this.assessmentCategories = new HashSet<>(assessmentCategories);
    }
}