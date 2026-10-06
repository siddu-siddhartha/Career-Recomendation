package com.example.backend.assistant;

import java.security.Principal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) { this.chatService = chatService; }

    @PostMapping
    public ChatDtos.Response reply(Principal principal, @Valid @RequestBody ChatDtos.Request request) {
        return chatService.reply(chatService.profileContext(principal.getName()), request);
    }
}
