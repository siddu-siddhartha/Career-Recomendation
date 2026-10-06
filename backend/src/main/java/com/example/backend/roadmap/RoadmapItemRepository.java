package com.example.backend.roadmap;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Long> {
    List<RoadmapItem> findByEmailAndRoleIdOrderById(String email, Long roleId);
    Optional<RoadmapItem> findByIdAndEmail(Long id, String email);
}