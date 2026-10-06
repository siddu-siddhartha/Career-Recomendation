package com.example.backend.profile;

import java.security.Principal;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final UserProfileRepository profiles;

    public ProfileController(UserProfileRepository profiles) { this.profiles = profiles; }

    @GetMapping
    @Transactional(readOnly = true)
    public ProfileDtos.Response get(Principal principal) { return response(find(principal)); }

    @PutMapping
    @Transactional
    public ProfileDtos.Response update(Principal principal, @RequestBody ProfileDtos.UpdateRequest request) {
        UserProfile profile = find(principal);
        var skills = request.skills() == null ? null : request.skills().stream()
                .map(skill -> new UserSkill(skill.name().trim(), skill.proficiency()))
                .collect(Collectors.toSet());
        profile.updateProfile(request.education(), request.experienceYears(), request.workStyle(),
            request.preferredIndustry(), skills, request.interests(), request.academicScore(),
            request.assessmentScores());
        return response(profiles.save(profile));
    }

    private UserProfile find(Principal principal) {
        return profiles.findByEmailIgnoreCase(principal.getName()).orElseThrow();
    }

    private ProfileDtos.Response response(UserProfile profile) {
        var skills = profile.getSkills().stream()
                .map(skill -> new ProfileDtos.SkillInput(skill.getName(), skill.getProficiency()))
                .collect(Collectors.toSet());
        return new ProfileDtos.Response(profile.getId(), profile.getEmail(), profile.getEducation(),
                profile.getExperienceYears(), profile.getWorkStyle(), profile.getPreferredIndustry(),
            skills, profile.getInterests(), profile.getAcademicScore(), profile.getAssessmentScores());
    }
}