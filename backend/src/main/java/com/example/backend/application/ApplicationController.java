package com.example.backend.application;

import java.security.Principal;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final CareerApplicationRepository applications;

    public ApplicationController(CareerApplicationRepository applications) {
        this.applications = applications;
    }

    @GetMapping
    public List<ApplicationDtos.Response> list(Principal principal) {
        return applications.findByEmailOrderByCreatedAtDesc(principal.getName()).stream()
                .map(this::response)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationDtos.Response create(Principal principal,
                                           @Valid @RequestBody ApplicationDtos.CreateRequest request) {
        CareerApplication application = new CareerApplication(principal.getName(), request.role().trim(),
                request.company().trim(), blankToNull(request.postingUrl()), ApplicationStatus.SAVED,
                request.appliedDate(), blankToNull(request.notes()));
        return response(applications.save(application));
    }

    @PatchMapping("/{id}")
    public ApplicationDtos.Response update(Principal principal, @PathVariable Long id,
                                           @Valid @RequestBody ApplicationDtos.UpdateRequest request) {
        CareerApplication application = findOwned(id, principal.getName());
        application.update(request.status(), request.appliedDate(), blankToNull(request.notes()));
        return response(applications.save(application));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable Long id) {
        applications.delete(findOwned(id, principal.getName()));
    }

    private CareerApplication findOwned(Long id, String email) {
        return applications.findByIdAndEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));
    }

    private ApplicationDtos.Response response(CareerApplication application) {
        return new ApplicationDtos.Response(application.getId(), application.getRole(), application.getCompany(),
                application.getPostingUrl(), application.getStatus(), application.getAppliedDate(),
                application.getNotes(), application.getCreatedAt());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}