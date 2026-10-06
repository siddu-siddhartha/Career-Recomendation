package com.example.backend.recommendation;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.profile.Proficiency;
import com.example.backend.profile.UserProfile;
import com.example.backend.profile.UserProfileRepository;
import com.example.backend.roadmap.RoadmapItem;
import com.example.backend.roadmap.RoadmapItemRepository;
import com.example.backend.role.CareerRole;
import com.example.backend.role.CareerRoleRepository;
import com.example.backend.role.RequiredSkill;

@Service
public class RecommendationService {
    private final UserProfileRepository profiles;
    private final CareerRoleRepository roles;
    private final RecommendationSnapshotRepository snapshots;
        private final RoadmapItemRepository roadmapItems;

    public RecommendationService(UserProfileRepository profiles, CareerRoleRepository roles,
                                                                 RecommendationSnapshotRepository snapshots, RoadmapItemRepository roadmapItems) {
        this.profiles = profiles;
        this.roles = roles;
        this.snapshots = snapshots;
                this.roadmapItems = roadmapItems;
    }

    @Transactional
    public List<RecommendationDtos.Match> recommend(String email) {
        UserProfile profile = profile(email);
        List<RecommendationDtos.Match> results = roles.findAll().stream()
                .map(role -> match(profile, role))
                .sorted(Comparator.comparingDouble(RecommendationDtos.Match::matchScore).reversed())
                .toList();
        results.forEach(result -> snapshots.save(new RecommendationSnapshot(email, result.roleId(), result.matchScore())));
        return results;
    }

        @Transactional
    public RecommendationDtos.RoleDetail detail(String email, Long roleId) {
        UserProfile profile = profile(email);
        CareerRole role = roles.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        RecommendationDtos.Match match = match(profile, role);
        Map<String, Proficiency> skillMap = profile.getSkills().stream()
                .collect(Collectors.toMap(skill -> normalize(skill.getName()),
                        skill -> skill.getProficiency(), (first, second) -> second));
        List<RecommendationDtos.Gap> gaps = role.getRequiredSkills().stream()
                .map(required -> gap(required, skillMap.get(normalize(required.getName()))))
                .sorted(Comparator.comparingInt(RecommendationDtos.Gap::priority).reversed())
                .toList();
        List<RecommendationDtos.RoadmapItem> roadmap = gaps.stream()
                .filter(gap -> !"STRONG".equals(gap.status()))
                .map(gap -> new RecommendationDtos.RoadmapItem(gap.skill(),
                        "Build " + gap.skill() + " capability", "PROJECT",
                        gap.suggestion()))
                .toList();
        if (roadmapItems.findByEmailAndRoleIdOrderById(email, roleId).isEmpty()) {
            roadmapItems.saveAll(roadmap.stream().map(item -> new RoadmapItem(email, roleId, item.skill(),
                    item.title(), item.nextStep())).toList());
        }
        return new RecommendationDtos.RoleDetail(match, gaps, roadmap);
    }

    @Transactional(readOnly = true)
    public List<RecommendationDtos.Snapshot> history(String email) {
        return snapshots.findTop20ByEmailOrderByGeneratedAtDesc(email).stream()
                .map(snapshot -> new RecommendationDtos.Snapshot(snapshot.getId(), snapshot.getRoleId(),
                        snapshot.getMatchScore(), snapshot.getGeneratedAt().toString()))
                .toList();
    }

    private RecommendationDtos.Match match(UserProfile profile, CareerRole role) {
        Map<String, Proficiency> userSkills = profile.getSkills().stream()
                .collect(Collectors.toMap(skill -> normalize(skill.getName()),
                        skill -> skill.getProficiency(), (first, second) -> second));
        int totalWeight = role.getRequiredSkills().stream().mapToInt(RequiredSkill::getWeight).sum();
        double skillScore = totalWeight == 0 ? 0 : role.getRequiredSkills().stream()
                .mapToDouble(skill -> skill.getWeight() * proficiencyFactor(userSkills.get(normalize(skill.getName()))))
                .sum() / totalWeight;
        double interestScore = overlap(profile.getInterests(), role.getInterests());
        double educationScore = educationFit(profile.getEducation(), role.getMinEducation());
        double quizScore = assessmentFit(profile, role);
        double academicScore = profile.getAcademicScore() == null ? .5 : profile.getAcademicScore() / 100.0;
        double score = Math.round((skillScore * .45 + interestScore * .15 + educationScore * .15
                + quizScore * .2 + academicScore * .05) * 1000) / 10.0;
        List<String> matched = role.getRequiredSkills().stream()
                .filter(skill -> userSkills.containsKey(normalize(skill.getName())))
                .map(RequiredSkill::getName).toList();
        List<String> missing = role.getRequiredSkills().stream()
                .filter(skill -> !userSkills.containsKey(normalize(skill.getName())))
                .map(RequiredSkill::getName).toList();
                return new RecommendationDtos.Match(role.getId(), role.getName(), role.getDescription(),
                                role.getIndustry(), score, matched, missing, percent(skillScore), percent(interestScore),
                                percent(educationScore), percent(quizScore), percent(academicScore));
    }

        private double assessmentFit(UserProfile profile, CareerRole role) {
                double average = role.getAssessmentCategories().stream()
                                .map(profile.getAssessmentScores()::get)
                                .filter(value -> value != null)
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(50.0);
                return average / 100.0;
        }

        private double percent(double value) {
                return Math.round(value * 1000) / 10.0;
        }

    private RecommendationDtos.Gap gap(RequiredSkill required, Proficiency proficiency) {
        if (proficiency == null) {
            return new RecommendationDtos.Gap(required.getName(), "MISSING", required.getWeight(),
                    "Complete a guided course and build a small project using " + required.getName());
        }
        String status = proficiency == Proficiency.BEGINNER ? "WEAK" : "STRONG";
        return new RecommendationDtos.Gap(required.getName(), status, required.getWeight(),
                status.equals("WEAK") ? "Practice " + required.getName() + " through a portfolio project" : "Keep this skill current");
    }

    private double overlap(Set<String> userValues, Set<String> roleValues) {
        if (roleValues.isEmpty()) return .5;
        long matches = roleValues.stream().map(this::normalize).filter(value -> userValues.stream()
                .map(this::normalize).anyMatch(value::equals)).count();
        return (double) matches / roleValues.size();
    }

    private double educationFit(String userEducation, String requiredEducation) {
        if (requiredEducation == null || requiredEducation.isBlank()) return 1;
        List<String> levels = List.of("high school", "associate", "bachelor", "master", "doctorate");
        int user = indexOf(levels, userEducation);
        int required = indexOf(levels, requiredEducation);
        return user >= required ? 1 : .35;
    }

    private int indexOf(List<String> levels, String value) {
        String normalized = normalize(value);
        return levels.stream().filter(normalized::contains).findFirst().map(levels::indexOf).orElse(0);
    }

    private double proficiencyFactor(Proficiency proficiency) {
        return proficiency == null ? 0 : switch (proficiency) {
            case BEGINNER -> .35;
            case INTERMEDIATE -> .65;
            case ADVANCED -> .85;
            case EXPERT -> 1;
        };
    }

    private UserProfile profile(String email) {
        return profiles.findByEmailIgnoreCase(email).orElseThrow();
    }

    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
}