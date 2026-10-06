package com.example.backend.role;

import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class RequiredSkill {
    private String name;
    private int weight;

    protected RequiredSkill() {
    }

    public RequiredSkill(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public String getName() { return name; }
    public int getWeight() { return weight; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof RequiredSkill skill)) return false;
        return weight == skill.weight && Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, weight);
    }
}