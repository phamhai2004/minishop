package com.example.minishop.entity;

import com.example.minishop.constant.ChatMessageType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(
                        name = "idx_chat_message_conversation",
                        columnList = "conversation_id"
                ),
                @Index(
                        name = "idx_chat_message_sender",
                        columnList = "sender_id"
                ),
                @Index(
                        name = "idx_chat_message_conversation_created",
                        columnList =
                                "conversation_id, created_at"
                )
        }
)
public class ChatMessage {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "conversation_id",
            nullable = false
    )
    private ChatConversation conversation;
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "sender_id",
            nullable = false
    )
    private User sender;
    @Enumerated(EnumType.STRING)
    @Column(
            name = "message_type",
            length = 20
    )
    private ChatMessageType type =
            ChatMessageType.TEXT;
    @ManyToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "product_id"
    )
    private Product product;
    @Column(
            nullable = false,
            length = 2000
    )
    private String content;
    @Column(nullable = false)
    private Boolean readStatus = false;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt =
                    LocalDateTime.now();
        }

        if (readStatus == null) {
            readStatus = false;
        }

        if (type == null) {
            type = ChatMessageType.TEXT;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(
            Long id
    ) {
        this.id = id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public void setConversation(
            ChatConversation conversation
    ) {
        this.conversation =
                conversation;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(
            User sender
    ) {
        this.sender = sender;
    }

    public ChatMessageType getType() {
        return type;
    }

    public void setType(ChatMessageType type) {
        this.type = type;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
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
        this.readStatus =
                readStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
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