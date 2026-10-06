package com.example.backend.profile;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserSkill {
    private String name;
    private Proficiency proficiency;

    protected UserSkill() {
    }

    public UserSkill(String name, Proficiency proficiency) {
        this.name = name;
        this.proficiency = proficiency;
    }

    public String getName() { return name; }
    public Proficiency getProficiency() { return proficiency; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof UserSkill skill)) return false;
        return Objects.equals(name, skill.name) && proficiency == skill.proficiency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, proficiency);
    }
}