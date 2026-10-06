package com.example.backend.roadmap;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {
    private final RoadmapItemRepository items;
    private final RoadmapChallengeService challenges;

    public RoadmapController(RoadmapItemRepository items, RoadmapChallengeService challenges) {
        this.items = items;
        this.challenges = challenges;
    }

    @GetMapping("/{roleId}")
    public List<RoadmapDtos.Item> get(Principal principal, @PathVariable Long roleId) {
        return items.findByEmailAndRoleIdOrderById(principal.getName(), roleId).stream()
                .map(item -> new RoadmapDtos.Item(item.getId(), item.getRoleId(), item.getSkill(), item.getTitle(),
                        item.getNextStep(), item.isCompleted(), item.getCreatedAt(), item.getCompletedAt(),
                        challenges.challengeFor(item)))
                .toList();
    }

    @PatchMapping("/{itemId}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void complete(Principal principal, @PathVariable Long itemId,
                         @Valid @RequestBody RoadmapDtos.CompletionRequest request) {
        RoadmapItem item = items.findByIdAndEmail(itemId, principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Roadmap item not found"));
        if (!challenges.isCorrect(item, request.answer())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "That answer is not correct yet. Review the problem and try again.");
        }
        item.complete();
        items.save(item);
    }
}