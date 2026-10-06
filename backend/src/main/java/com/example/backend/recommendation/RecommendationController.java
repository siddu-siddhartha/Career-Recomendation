package com.example.backend.recommendation;

import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) { this.service = service; }

    @GetMapping
    public List<RecommendationDtos.Match> recommend(Principal principal) { return service.recommend(principal.getName()); }

    @GetMapping("/history")
    public List<RecommendationDtos.Snapshot> history(Principal principal) { return service.history(principal.getName()); }

    @GetMapping("/roles/{roleId}")
    public RecommendationDtos.RoleDetail detail(Principal principal, @PathVariable Long roleId) {
        return service.detail(principal.getName(), roleId);
    }
}