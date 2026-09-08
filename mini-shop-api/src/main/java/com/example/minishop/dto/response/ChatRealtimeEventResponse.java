package com.example.minishop.dto.response;

import java.time.LocalDateTime;

public class ChatRealtimeEventResponse {

    private String type;
    private Long conversationId;
    private Long actorUserId;
    private ChatMessageResponse message;
    private LocalDateTime occurredAt;

    public ChatRealtimeEventResponse() {
    }

    public ChatRealtimeEventResponse(
            String type,
            Long conversationId,
            Long actorUserId,
            ChatMessageResponse message
    ) {
        this.type = type;
        this.conversationId = conversationId;
        this.actorUserId = actorUserId;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    public String getType() {
        return type;
    }

    public void setType(
            String type
    ) {
        this.type = type;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(
            Long conversationId
    ) {
        this.conversationId =
                conversationId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(
            Long actorUserId
    ) {
        this.actorUserId =
                actorUserId;
    }

    public ChatMessageResponse getMessage() {
        return message;
    }

    public void setMessage(
            ChatMessageResponse message
    ) {
        this.message = message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(
            LocalDateTime occurredAt
    ) {
        this.occurredAt = occurredAt;
    }
}