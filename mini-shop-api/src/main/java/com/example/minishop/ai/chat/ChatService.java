package com.example.minishop.ai.chat;

import com.example.minishop.ai.dto.ChatResponse;

public interface ChatService {

    ChatResponse chat(Long userId, String message);
}