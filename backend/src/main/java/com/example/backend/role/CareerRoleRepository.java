package com.example.backend.role;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRoleRepository extends JpaRepository<CareerRole, Long> {
	@EntityGraph(attributePaths = {"requiredSkills", "interests", "assessmentCategories"})
	@Override
	List<CareerRole> findAll();
	boolean existsByNameIgnoreCase(String name);
	java.util.Optional<CareerRole> findByNameIgnoreCase(String name);
}