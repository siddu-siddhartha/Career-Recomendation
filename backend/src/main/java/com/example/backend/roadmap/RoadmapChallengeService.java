package com.example.backend.roadmap;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class RoadmapChallengeService {
    public RoadmapDtos.Challenge challengeFor(RoadmapItem item) {
        return definitionFor(item).challenge();
    }

    public boolean isCorrect(RoadmapItem item, String answer) {
        return definitionFor(item).correctOption().equalsIgnoreCase(answer.trim());
    }

    private ChallengeDefinition definitionFor(RoadmapItem item) {
        String skill = item.getSkill().trim().toLowerCase(Locale.ROOT);
        if (skill.equals("sql")) {
            return new ChallengeDefinition(new RoadmapDtos.Challenge(
                    "The orders table has customer_id and amount. Which query returns each customer's total spend, highest first?",
                    List.of(
                            "SELECT customer_id, SUM(amount) AS total FROM orders GROUP BY customer_id ORDER BY total DESC;",
                            "SELECT customer_id, SUM(amount) FROM orders ORDER BY amount DESC;",
                            "SELECT customer_id, amount FROM orders GROUP BY customer_id;",
                            "SELECT SUM(amount) FROM orders ORDER BY customer_id;")), "A");
        }
        if (skill.contains("git")) {
            return new ChallengeDefinition(new RoadmapDtos.Challenge(
                    "Which command stages only the file report.sql for your next commit?",
                    List.of("git add report.sql", "git commit report.sql", "git push report.sql", "git status report.sql")), "A");
        }
        if (skill.equals("python")) {
            return new ChallengeDefinition(new RoadmapDtos.Challenge(
                    "Which expression creates a list containing only positive values from values?",
                    List.of("[value for value in values if value > 0]", "values.sort()", "values[0]", "sum(values)")), "A");
        }
        if (skill.equals("java")) {
            return new ChallengeDefinition(new RoadmapDtos.Challenge(
                    "What is true about a list created with List.of(\"a\", \"b\")?",
                    List.of("It is unmodifiable.", "It accepts null elements.", "It is a mutable ArrayList.", "It sorts elements automatically.")), "A");
        }
        if (skill.equals("statistics")) {
            return new ChallengeDefinition(new RoadmapDtos.Challenge(
                    "A 95% confidence interval for a mean is 12 to 18. Which interpretation is best?",
                    List.of("The method would capture the true mean in about 95% of repeated samples.",
                            "95% of individual values are between 12 and 18.",
                            "The true mean has a 95% chance of changing to a value in the interval.",
                            "The sample mean is exactly 15 in every sample.")), "A");
        }

        return new ChallengeDefinition(new RoadmapDtos.Challenge(
                "For " + item.getSkill() + ", which action best completes this task: " + item.getNextStep() + "?",
                List.of("Apply the skill in a small project, record the result, and check that it works.",
                        "Add the skill to a resume without practicing it.",
                        "Skip the task and assume the skill is mastered.",
                        "Copy an example without understanding or testing it.")), "A");
    }

    private record ChallengeDefinition(RoadmapDtos.Challenge challenge, String correctOption) {}
}