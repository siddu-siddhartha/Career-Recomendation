package com.example.backend.resume;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeRepository resumes;

    public ResumeController(ResumeRepository resumes) {
        this.resumes = resumes;
    }

    @GetMapping
    public List<ResumeDtos.Response> list(Principal principal) {
        return resumes.findByEmailOrderByUpdatedAtDesc(principal.getName()).stream()
                .map(this::response)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResumeDtos.Response create(Principal principal, @Valid @RequestBody ResumeDtos.SaveRequest request) {
        ResumeDocument resume = new ResumeDocument(principal.getName(), request.name().trim(),
                blankToNull(request.targetRole()), request.content().trim());
        return response(resumes.save(resume));
    }

    @PutMapping("/{id}")
    public ResumeDtos.Response update(Principal principal, @PathVariable Long id,
                                      @Valid @RequestBody ResumeDtos.SaveRequest request) {
        ResumeDocument resume = findOwned(id, principal.getName());
        resume.update(request.name().trim(), blankToNull(request.targetRole()), request.content().trim());
        return response(resumes.save(resume));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable Long id) {
        resumes.delete(findOwned(id, principal.getName()));
    }

    private ResumeDocument findOwned(Long id, String email) {
        return resumes.findByIdAndEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
    }

    private ResumeDtos.Response response(ResumeDocument resume) {
        return new ResumeDtos.Response(resume.getId(), resume.getName(), resume.getTargetRole(), resume.getContent(),
                resume.getCreatedAt(), resume.getUpdatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}