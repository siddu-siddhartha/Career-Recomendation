package com.example.backend.assistant;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.example.backend.profile.Proficiency;
import com.example.backend.profile.UserProfile;
import com.example.backend.profile.UserProfileRepository;
import com.example.backend.role.CareerRole;
import com.example.backend.role.CareerRoleRepository;

@Service
public class ChatService {
    private final UserProfileRepository profiles;
    private final CareerRoleRepository roles;
    private final RestClient http = RestClient.create();
    private final String apiKey;
    private final String model;

    public ChatService(UserProfileRepository profiles, CareerRoleRepository roles,
                       @Value("${app.gemini.api-key:}") String apiKey,
                       @Value("${app.gemini.model:gemini-3.8-flash}") String model) {
        this.profiles = profiles;
        this.roles = roles;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Transactional(readOnly = true)
    public ProfileContext profileContext(String email) {
        UserProfile profile = profiles.findByEmailIgnoreCase(email).orElseThrow();
        List<String> skills = profile.getSkills().stream()
                .map(skill -> skill.getName() + " (" + proficiency(skill.getProficiency()) + ")")
                .distinct()
                .sorted()
                .toList();
        List<RoleContext> careerRoles = roles.findAll().stream().map(this::roleContext).toList();
        return new ProfileContext(profile.getEducation(), profile.getExperienceYears(), profile.getWorkStyle(),
            profile.getPreferredIndustry(), skills, profile.getInterests().stream().sorted().toList(),
            profile.getAcademicScore(), Map.copyOf(profile.getAssessmentScores()), careerRoles);
    }

    private RoleContext roleContext(CareerRole role) {
        List<SkillContext> requiredSkills = role.getRequiredSkills().stream()
            .map(skill -> new SkillContext(skill.getName(), skill.getWeight()))
            .distinct()
            .sorted(java.util.Comparator.comparingInt(SkillContext::weight).reversed())
            .toList();
        return new RoleContext(role.getName(), role.getDescription(), role.getIndustry(), role.getMinEducation(),
            requiredSkills, role.getInterests().stream().distinct().sorted().toList(), role.getAssessmentCategories());
        }

    public ChatDtos.Response reply(ProfileContext profile, ChatDtos.Request request) {
        ChatDtos.Message latest = request.messages().get(request.messages().size() - 1);
        if (!"user".equals(latest.role())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "The last chat message must be from the user");
        }
        if (apiKey.isBlank()) return new ChatDtos.Response(
            localAnswer(profile, latest.content(), request.messages()), "local");

        List<Map<String, Object>> contents = request.messages().stream()
                .map(message -> Map.<String, Object>of("role", message.role(),
                        "parts", List.of(Map.of("text", message.content()))))
                .toList();
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction(profile)))),
                "contents", contents,
                "generationConfig", Map.of("temperature", 0.5, "maxOutputTokens", 600));
        try {
            Map<String, Object> result = http.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent", model)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<>() {});
            String answer = responseText(result);
            if (answer == null || answer.isBlank()) {
                return new ChatDtos.Response(localAnswer(profile, latest.content(), request.messages()), "local");
            }
            return new ChatDtos.Response(answer, "gemini");
        } catch (RestClientResponseException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_GATEWAY,
                    "The AI guide is unavailable right now. Check the Gemini key and model configuration.");
        }
    }

    private String responseText(Map<String, Object> result) {
        if (result == null || !(result.get("candidates") instanceof List<?> candidates) || candidates.isEmpty()) return null;
        if (!(candidates.get(0) instanceof Map<?, ?> candidate)
                || !(candidate.get("content") instanceof Map<?, ?> content)
                || !(content.get("parts") instanceof List<?> parts) || parts.isEmpty()
                || !(parts.get(0) instanceof Map<?, ?> part)) return null;
        Object text = part.get("text");
        return text instanceof String value ? value : null;
    }

    private String systemInstruction(ProfileContext profile) {
        return """
                You are Northstar, a practical career guidance assistant. Answer the user's career, skills,
                education, and job-search questions in clear, supportive language. Do not promise a job or
                treat a match score as a prediction. Explain that the app's ranking is a heuristic. Use the
                profile below as context, never ask for passwords or secrets, and do not invent qualifications.
                Profile context: education=%s; experience=%s years; work style=%s; preferred industry=%s;
                skills=%s; interests=%s; academic score=%s; quiz category scores=%s
                """.formatted(value(profile.education()), value(profile.experienceYears()), value(profile.workStyle()),
                value(profile.preferredIndustry()), profile.skills(), profile.interests(),
                value(profile.academicScore()), profile.assessmentScores());
    }

    private String localAnswer(ProfileContext profile, String question, List<ChatDtos.Message> conversation) {
        String prompt = normalize(question);
        List<RoleMatch> matches = rankedRoles(profile);
        RoleMatch best = matches.isEmpty() ? null : matches.get(0);

        if (prompt.matches("^(hi|hello|hey|good morning|good afternoon|good evening)[!.?, ]*$")) {
            return choose(conversation,
                    "Hi! I can help you read your career matches, choose a skill to build, or turn a role into a project plan. What are you working toward?",
                    "Hello! Your career guide is ready. Ask about a role, your quiz, grades, CV, or what to learn next.");
        }
        if (containsAny(prompt, "thank", "thanks", "appreciate")) {
            return choose(conversation,
                    "You're welcome. A useful next move is to pick one role and turn its biggest skill gap into a small project this week.",
                    "Glad to help. You can ask a follow-up about the role, skill, or quiz result we just discussed.");
        }
        if (containsAny(prompt, "quiz", "assessment", "personality")) {
            if (profile.assessmentScores().isEmpty()) {
                return "Your quiz has six work-preference areas: technical, analytical, creative, people-focused, leadership, and hands-on. I don't have your answers yet; complete Career Quiz and save the results, then I can explain which areas stand out.";
            }
            String strongest = profile.assessmentScores().entrySet().stream()
                    .max(Map.Entry.comparingByValue()).map(entry -> categoryName(entry.getKey()) + " (" + entry.getValue() + "%)")
                    .orElse("not available");
            String developing = profile.assessmentScores().entrySet().stream()
                    .min(Map.Entry.comparingByValue()).map(entry -> categoryName(entry.getKey()) + " (" + entry.getValue() + "%)")
                    .orElse("not available");
            return choose(conversation,
                    "Your strongest quiz signal is " + strongest + "; " + developing + " is the area to explore more. These are preferences, not limits. Compare the top roles with what you actually enjoy doing.",
                    "From your saved quiz, " + strongest + " stands out most, while " + developing + " scores lowest. That can help sort career options, but it shouldn't rule a path out.");
        }
        if (containsAny(prompt, "mark", "grade", "gpa", "percentage", "academic")) {
            if (profile.academicScore() == null) {
                return "You haven't added an academic percentage yet. You can enter it in My Profile; it has a small 5% weight in matching, so skills, interests, education, and quiz preferences matter much more. You can leave it blank if you prefer.";
            }
            return "Your profile has an academic score of " + profile.academicScore() + "/100. It contributes only 5% to the match, so it won't outweigh your skills, interests, education, or quiz. For a stronger application, pair your coursework with a project that demonstrates the role's practical skills.";
        }
        if (containsAny(prompt, "resume", "résumé", "cv", "curriculum vitae")) {
            if (best == null) return "For a useful CV, choose one target role first. Add a short profile, relevant skills, education, and two evidence-based project or work examples. Add your target role and I can tailor the bullet points.";
            List<String> relevantSkills = sharedSkills(profile, best.role());
            return "For a " + best.role().name() + " CV, put " + (relevantSkills.isEmpty() ? "your closest project or coursework" : String.join(", ", relevantSkills))
                    + " near the top. Write each project bullet as action + tool + measurable result; don't claim skills or outcomes you can't support. The role also asks for " + formatSkills(missingSkills(profile, best.role())) + ".";
        }
        if (containsAny(prompt, "interview", "interviewing", "behavioral")) {
            String roleName = best == null ? "your target role" : best.role().name();
            return choose(conversation,
                    "For a " + roleName + " interview, prepare three short STAR stories: a problem you solved, a time you learned quickly, and a time you worked with others. Use a real result and be clear about your own contribution.",
                    "A practical " + roleName + " interview exercise: explain one project in two minutes, then cover the problem, your decision, the result, and what you'd improve next time. Which interview stage are you preparing for?");
        }
        if (containsAny(prompt, "salary", "pay", "income", "earnings", "compensation")) {
            return "I don't have a live salary dataset, so I shouldn't invent a pay range. Compensation varies by location, seniority, and employer. Compare recent postings and official labor statistics for your region; tell me the role and country and I can help you make a comparison checklist.";
        }
        if (containsAny(prompt, "project", "portfolio", "roadmap", "practice", "build")) {
            if (best == null) return "Choose one role from Career Matches, then build a small project that uses two of its required skills. Keep the scope to a weekend, document your decisions, and publish a short demo or write-up. Tell me a target role and I can make the project more specific.";
            List<String> missing = missingSkills(profile, best.role());
            String focus = missing.isEmpty() ? best.role().requiredSkills().stream().findFirst().map(SkillContext::name).orElse("a role skill") : missing.get(0);
            return "For " + best.role().name() + ", try a small " + focus + " project: solve one concrete problem, show your process, and write a short README with screenshots or results. " + (missing.size() > 1 ? "After that, extend it with " + missing.get(1) + "." : "Then add it to your portfolio and explain what you learned.");
        }
        if (containsAny(prompt, "learn", "study", "skill", "improve", "gap")) {
            if (best == null) return "Add your skills and interests in My Profile so I can point to a real gap. In the meantime, pick one skill from a role you want and practice it in a small project rather than collecting courses without applying them.";
            List<String> missing = missingSkills(profile, best.role());
            if (missing.isEmpty()) return "Your listed skills already cover the required skills for " + best.role().name() + ". To grow from here, deepen one skill with a more realistic project or explore a neighboring role with a different skill mix.";
            return choose(conversation,
                    "For " + best.role().name() + ", the highest-priority unlisted skill is " + missing.get(0) + ". Learn the basics, then prove them in a small project; your next gap is " + (missing.size() > 1 ? missing.get(1) : "none listed") + ".",
                    "I'd focus first on " + missing.get(0) + " for " + best.role().name() + ". A portfolio task using it is more useful than just adding another course certificate. Your current skills are " + formatSkills(sharedSkills(profile, best.role())) + ".");
        }
        if (containsAny(prompt, "interest", "enjoy", "like doing", "suit me", "fit me")) {
            String interestText = profile.interests().isEmpty() ? "You haven't added interests yet" : "Your listed interests are " + formatSkills(profile.interests());
            return best == null
                    ? interestText + ". Add a few interests and take Career Quiz so I can compare them with the role catalogue."
                    : interestText + ". Based on your current profile, I'd explore " + best.role().name() + " first, followed by " + (matches.size() > 1 ? matches.get(1).role().name() : "a related role") + ". Try a small task from each before deciding which work feels better.";
        }
        if (containsAny(prompt, "recommend", "career", "which role", "which job", "match", "best role")) {
            if (matches.isEmpty()) return "I couldn't find any career roles in the catalogue right now. Try refreshing Career Matches or ask again in a moment.";
            String topRoles = matches.stream().limit(3)
                    .map(match -> match.role().name() + " (" + match.role().industry() + ")")
                    .collect(java.util.stream.Collectors.joining(", "));
            return "Based on your current skills, interests, education, and quiz signals, start by comparing " + topRoles + ". "
                    + (best == null ? "Add more profile details to make this shortlist more personal." : "For " + best.role().name() + ", the next skills to build are " + formatSkills(missingSkills(profile, best.role())) + ".")
                    + " These are heuristic suggestions, not hiring predictions.";
        }
        if (containsAny(prompt, "education", "degree", "college", "experience", "work style")) {
            return "Your profile currently lists education as " + value(profile.education()) + ", experience as "
                    + value(profile.experienceYears()) + " years, and work style as " + value(profile.workStyle())
                    + ". You can update those in My Profile. For matching, evidence of relevant skills is often more actionable than the degree title alone.";
        }

        String focus = best == null ? "your career options" : best.role().name();
        return choose(conversation,
                "I read your question as: “" + question.trim() + "”. I can make a more useful answer if you tell me whether you're deciding on a role, choosing a skill to learn, or preparing an application. Right now, " + focus + " is one profile-based option to explore.",
                "For that, I'd start with " + focus + " and one concrete next step: compare its required skills with what you've already practiced. Which part would you like to work on first?");
    }

    private List<RoleMatch> rankedRoles(ProfileContext profile) {
        return profile.careerRoles().stream()
                .map(role -> new RoleMatch(role, matchScore(profile, role)))
                .sorted(java.util.Comparator.comparingDouble(RoleMatch::score).reversed())
                .toList();
    }

    private double matchScore(ProfileContext profile, RoleContext role) {
        int totalWeight = role.requiredSkills().stream().mapToInt(SkillContext::weight).sum();
        double skillFit = totalWeight == 0 ? 0 : role.requiredSkills().stream()
                .mapToDouble(skill -> skill.weight() * skillFactor(profile, skill.name())).sum() / totalWeight;
        double interestFit = role.interests().isEmpty() ? .5 : role.interests().stream()
                .map(this::normalize).filter(roleInterest -> profile.interests().stream()
                        .map(this::normalize).anyMatch(userInterest -> roleInterest.equals(userInterest)
                                || roleInterest.contains(userInterest) || userInterest.contains(roleInterest)))
                .count() / (double) role.interests().size();
        double quizFit = role.assessmentCategories().stream().map(profile.assessmentScores()::get)
                .filter(java.util.Objects::nonNull).mapToInt(Integer::intValue).average().orElse(50) / 100.0;
        double educationFit = educationFit(profile.education(), role.minEducation());
        double academicFit = profile.academicScore() == null ? .5 : profile.academicScore() / 100.0;
        return skillFit * .45 + interestFit * .15 + educationFit * .15 + quizFit * .2 + academicFit * .05;
    }

    private double skillFactor(ProfileContext profile, String requiredSkill) {
        return profile.skills().stream().filter(skill -> normalize(skillName(skill)).equals(normalize(requiredSkill)))
                .findFirst().map(skill -> {
                    String level = skill.substring(skill.indexOf('(') + 1).replace(")", "").trim().toLowerCase(java.util.Locale.ROOT);
                    return switch (level) {
                        case "beginner" -> .35;
                        case "intermediate" -> .65;
                        case "advanced" -> .85;
                        case "expert" -> 1.0;
                        default -> .5;
                    };
                }).orElse(0.0);
    }

    private List<String> missingSkills(ProfileContext profile, RoleContext role) {
        return role.requiredSkills().stream()
                .filter(skill -> skillFactor(profile, skill.name()) == 0)
                .sorted(java.util.Comparator.comparingInt(SkillContext::weight).reversed())
                .map(SkillContext::name).toList();
    }

    private List<String> sharedSkills(ProfileContext profile, RoleContext role) {
        return role.requiredSkills().stream().map(SkillContext::name)
                .filter(skill -> skillFactor(profile, skill) > 0).toList();
    }

    private double educationFit(String education, String required) {
        if (required == null || required.isBlank()) return 1;
        List<String> levels = List.of("high school", "associate", "bachelor", "master", "doctorate");
        int userLevel = educationIndex(levels, education);
        int requiredLevel = educationIndex(levels, required);
        return userLevel >= requiredLevel ? 1 : .35;
    }

    private int educationIndex(List<String> levels, String education) {
        String normalized = normalize(education);
        for (int index = 0; index < levels.size(); index++) {
            if (normalized.contains(levels.get(index))) return index;
        }
        return 0;
    }

    private String skillName(String skill) {
        int proficiencyStart = skill.indexOf('(');
        return proficiencyStart < 0 ? skill : skill.substring(0, proficiencyStart).trim();
    }

    private String categoryName(String category) {
        return category.replace('-', ' ');
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(normalize(term))) return true;
        return false;
    }

    private String choose(List<ChatDtos.Message> conversation, String... options) {
        long userTurns = conversation.stream().filter(message -> "user".equals(message.role())).count();
        return options[(int) ((userTurns - 1) % options.length)];
    }

    private String formatSkills(List<String> skills) {
        return skills.isEmpty() ? "none listed yet" : String.join(", ", skills);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private String proficiency(Proficiency value) {
        return value == null ? "not rated" : value.name().toLowerCase(java.util.Locale.ROOT);
    }

    private String value(Object value) { return value == null ? "not provided" : value.toString(); }

    public record ProfileContext(String education, Integer experienceYears, String workStyle,
                                 String preferredIndustry, List<String> skills, List<String> interests,
                                 Integer academicScore, Map<String, Integer> assessmentScores,
                                 List<RoleContext> careerRoles) {}
    public record RoleContext(String name, String description, String industry, String minEducation,
                              List<SkillContext> requiredSkills, List<String> interests,
                              Set<String> assessmentCategories) {}
    public record SkillContext(String name, int weight) {}
    public record RoleMatch(RoleContext role, double score) {}
}
