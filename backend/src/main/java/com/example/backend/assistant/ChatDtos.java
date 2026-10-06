package com.example.backend.assistant;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ChatDtos {
    private ChatDtos() {}

    public record Message(@NotBlank @Pattern(regexp = "user|model") String role,
                          @NotBlank @Size(max = 1500) String content) {}

    public record Request(@NotEmpty @Size(max = 12) List<@Valid Message> messages) {}

    public record Response(String answer, String mode) {}
}
