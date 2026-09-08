package com.example.minishop.dto.response;

import com.example.minishop.constant.ChatMessageType;

import java.time.LocalDateTime;

public class ChatMessageResponse {

    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private ChatMessageType type;
    private ChatProductResponse product;
    private String content;
    private Boolean readStatus;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(
            Long senderId
    ) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(
            String senderName
    ) {
        this.senderName = senderName;
    }

    public ChatMessageType getType() {
        return type;
    }

    public void setType(ChatMessageType type) {
        this.type = type;
    }

    public ChatProductResponse getProduct() {
        return product;
    }

    public void setProduct(ChatProductResponse product) {
        this.product = product;
    }

    public String getContent() {
        return content;
    }

    public void setContent(
            String content
    ) {
        this.content = content;
    }

    public Boolean getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(
            Boolean readStatus
    ) {
        this.readStatus = readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(
            LocalDateTime readAt
    ) {
        this.readAt = readAt;
    }
}