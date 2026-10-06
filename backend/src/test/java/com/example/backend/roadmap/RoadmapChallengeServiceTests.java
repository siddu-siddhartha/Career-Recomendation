package com.example.backend.roadmap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RoadmapChallengeServiceTests {
    private final RoadmapChallengeService challenges = new RoadmapChallengeService();

    @Test
    void sqlChallengeRequiresTheCorrectChoice() {
        RoadmapItem item = new RoadmapItem("test@example.com", 1L, "SQL", "Build SQL capability", "Practice SQL");

        RoadmapDtos.Challenge challenge = challenges.challengeFor(item);

        assertTrue(challenge.prompt().contains("customer_id"));
        assertEquals(4, challenge.options().size());
        assertTrue(challenges.isCorrect(item, "A"));
        assertFalse(challenges.isCorrect(item, "B"));
    }

    @Test
    void completionTimestampIsSetOnlyOnce() {
        RoadmapItem item = new RoadmapItem("test@example.com", 1L, "SQL", "Build SQL capability", "Practice SQL");

        item.complete();
        var completedAt = item.getCompletedAt();
        item.complete();

        assertTrue(item.isCompleted());
        assertNotNull(completedAt);
        assertEquals(completedAt, item.getCompletedAt());
    }
}