package com.example.minishop.controller.customer;

import com.example.minishop.ai.chat.ChatService;
import com.example.minishop.ai.dto.ChatResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@SecurityRequirement(name = "bearerAuth")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {

        return chatService.chat(null, request.message());
    }

    public record ChatRequest(
            String message
    ) {}
}