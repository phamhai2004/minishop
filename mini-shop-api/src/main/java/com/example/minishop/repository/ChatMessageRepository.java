package com.example.minishop.repository;

import com.example.minishop.constant.ChatMessageType;
import com.example.minishop.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage>
    findByConversation_IdOrderByCreatedAtAsc(
            Long conversationId
    );

    Optional<ChatMessage>
    findFirstByConversation_IdOrderByCreatedAtDesc(
            Long conversationId
    );

    long countByConversation_IdAndSender_IdNotAndReadStatusFalse(
            Long conversationId,
            Long currentUserId
    );

    List<ChatMessage>
    findByConversation_IdAndSender_IdNotAndReadStatusFalse(
            Long conversationId,
            Long currentUserId
    );

    Optional<ChatMessage>
    findFirstByConversation_IdAndTypeOrderByCreatedAtDesc(
            Long conversationId,
            ChatMessageType type
    );
}