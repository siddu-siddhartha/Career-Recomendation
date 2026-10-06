package com.example.backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record SignupRequest(@Email @NotBlank String email, @Size(min = 8) String password) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
    public record AuthResponse(String token, ProfileSummary profile) {}
    public record ProfileSummary(Long id, String email, String education) {}
}