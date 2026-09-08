package com.example.minishop.controller;

import com.example.minishop.dto.common.ApiResponse;
import com.example.minishop.dto.request.SendChatMessageRequest;
import com.example.minishop.dto.response.ChatConversationResponse;
import com.example.minishop.dto.response.ChatMessageResponse;
import com.example.minishop.service.ShopChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shop-chat")
public class ShopChatController {

    private final ShopChatService
            shopChatService;

    public ShopChatController(
            ShopChatService shopChatService
    ) {
        this.shopChatService =
                shopChatService;
    }

    @PostMapping(
            "/conversations/shops/{shopId}"
    )
    public ResponseEntity<
            ApiResponse<ChatConversationResponse>
            >
    openConversation(
            @PathVariable Long shopId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopChatService
                                .openConversation(
                                        shopId
                                )
                )
        );
    }

    @GetMapping("/conversations")
    public ResponseEntity<
            ApiResponse<
                    List<ChatConversationResponse>
                    >
            >
    getMyConversations() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopChatService
                                .getMyConversations()
                )
        );
    }

    @GetMapping(
            "/conversations/{conversationId}/messages"
    )
    public ResponseEntity<
            ApiResponse<
                    List<ChatMessageResponse>
                    >
            >
    getMessages(
            @PathVariable
            Long conversationId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopChatService
                                .getMessages(
                                        conversationId
                                )
                )
        );
    }

    @PostMapping(
            "/conversations/{conversationId}/messages"
    )
    public ResponseEntity<
            ApiResponse<ChatMessageResponse>
            >
    sendMessage(
            @PathVariable
            Long conversationId,

            @Valid
            @RequestBody
            SendChatMessageRequest request
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gửi tin nhắn thành công",
                        shopChatService
                                .sendMessage(
                                        conversationId,
                                        request
                                )
                )
        );
    }

    @PatchMapping(
            "/conversations/{conversationId}/read"
    )
    public ResponseEntity<
            ApiResponse<Void>
            >
    markAsRead(
            @PathVariable
            Long conversationId
    ) {

        shopChatService.markAsRead(
                conversationId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã đánh dấu tin nhắn là đã đọc",
                        null
                )
        );
    }

    @PostMapping(
            "/conversations/shops/{shopId}/products/{productId}"
    )
    public ResponseEntity<
            ApiResponse<ChatConversationResponse>
            >
    openConversationWithProduct(
            @PathVariable Long shopId,
            @PathVariable Long productId
    ) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        shopChatService
                                .openConversationWithProduct(
                                        shopId,
                                        productId
                                )
                )
        );
    }

    @PatchMapping(
            "/conversations/{conversationId}/product-context/close"
    )
    public ResponseEntity<
            ApiResponse<Void>
            >
    closeProductContext(
            @PathVariable
            Long conversationId
    ) {

        shopChatService
                .closeProductContext(
                        conversationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đã đóng thông tin sản phẩm",
                        null
                )
        );
    }
}