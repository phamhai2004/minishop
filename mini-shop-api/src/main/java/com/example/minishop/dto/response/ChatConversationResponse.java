package com.example.minishop.dto.response;

import java.time.LocalDateTime;

public class ChatConversationResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long shopId;
    private String shopName;
    private String shopLogoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Long unreadCount;
    private ChatProductResponse contextProduct;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(
            Long customerId
    ) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(
            String customerName
    ) {
        this.customerName = customerName;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(
            Long shopId
    ) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(
            String shopName
    ) {
        this.shopName = shopName;
    }

    public String getShopLogoUrl() {
        return shopLogoUrl;
    }

    public void setShopLogoUrl(
            String shopLogoUrl
    ) {
        this.shopLogoUrl = shopLogoUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(
            String lastMessage
    ) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(
            LocalDateTime lastMessageAt
    ) {
        this.lastMessageAt =
                lastMessageAt;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(
            Long unreadCount
    ) {
        this.unreadCount = unreadCount;
    }

    public ChatProductResponse getContextProduct() {
        return contextProduct;
    }

    public void setContextProduct(ChatProductResponse contextProduct) {
        this.contextProduct = contextProduct;
    }
}