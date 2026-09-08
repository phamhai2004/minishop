package com.example.minishop.service;

import com.example.minishop.constant.ChatMessageType;
import com.example.minishop.constant.ProductStatus;
import com.example.minishop.constant.ShopStatus;
import com.example.minishop.dto.request.SendChatMessageRequest;
import com.example.minishop.dto.response.ChatConversationResponse;
import com.example.minishop.dto.response.ChatMessageResponse;
import com.example.minishop.dto.response.ChatProductResponse;
import com.example.minishop.dto.response.ChatRealtimeEventResponse;
import com.example.minishop.entity.*;
import com.example.minishop.exception.ResourceNotFoundException;
import com.example.minishop.repository.*;
import com.example.minishop.security.SecurityUtils;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShopChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ProductRepository productRepository;
    private final PricingService pricingService;

    public ShopChatService(
            ChatConversationRepository conversationRepository,
            ChatMessageRepository messageRepository,
            ShopRepository shopRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate,
            ProductRepository productRepository,
            PricingService pricingService
    ) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.productRepository = productRepository;
        this.pricingService = pricingService;
    }

    @Transactional
    public ChatConversationResponse
    openConversation(
            Long shopId
    ) {

        Long customerId =
                SecurityUtils
                        .getCurrentUserId();

        Shop shop =
                getActiveShop(shopId);

        ChatConversation conversation =
                getOrCreateConversation(
                        customerId,
                        shop
                );

        return toConversationResponse(
                conversation,
                customerId
        );
    }

    @Transactional(readOnly = true)
    public List<ChatConversationResponse>
    getMyConversations() {

        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        List<ChatConversation> asCustomer =
                conversationRepository
                        .findByCustomer_IdOrderByUpdatedAtDesc(
                                userId
                        );

        if (!asCustomer.isEmpty()) {

            return asCustomer
                    .stream()
                    .map(conversation ->
                            toConversationResponse(
                                    conversation,
                                    userId
                            )
                    )
                    .toList();
        }

        return conversationRepository
                .findByShop_Owner_IdOrderByUpdatedAtDesc(
                        userId
                )
                .stream()
                .map(conversation ->
                        toConversationResponse(
                                conversation,
                                userId
                        )
                )
                .toList();
    }

    private Shop getActiveShop(
            Long shopId
    ) {

        Shop shop =
                shopRepository
                        .findById(shopId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy shop"
                                        )
                        );

        if (
                shop.getStatus()
                        != ShopStatus.ACTIVE
        ) {
            throw new ResourceNotFoundException(
                    "Shop hiện không hoạt động"
            );
        }

        return shop;
    }

    private ChatConversation
    getOrCreateConversation(
            Long customerId,
            Shop shop
    ) {

        return conversationRepository
                .findByCustomer_IdAndShop_Id(
                        customerId,
                        shop.getId()
                )
                .orElseGet(() -> {

                    User customer =
                            userRepository
                                    .findById(
                                            customerId
                                    )
                                    .orElseThrow(
                                            () ->
                                                    new ResourceNotFoundException(
                                                            "Không tìm thấy khách hàng"
                                                    )
                                    );

                    ChatConversation conversation =
                            new ChatConversation();

                    conversation.setCustomer(
                            customer
                    );

                    conversation.setShop(
                            shop
                    );

                    return conversationRepository
                            .save(
                                    conversation
                            );
                });
    }

    @Transactional
    public ChatConversationResponse
    openConversationWithProduct(
            Long shopId,
            Long productId
    ) {

        Long customerId =
                SecurityUtils
                        .getCurrentUserId();

        Shop shop =
                getActiveShop(shopId);

        Product product =
                productRepository
                        .findByIdAndShop_Id(
                                productId,
                                shopId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Sản phẩm không thuộc shop này"
                                        )
                        );

        if (
                product.getStatus()
                        != ProductStatus.ACTIVE
        ) {
            throw new ResourceNotFoundException(
                    "Sản phẩm hiện không còn được bán"
            );
        }

        ChatConversation conversation =
                getOrCreateConversation(
                        customerId,
                        shop
                );
        conversation.setProductContextVisible(
                true
        );
        ChatMessage lastProductContext =
                messageRepository
                        .findFirstByConversation_IdAndTypeOrderByCreatedAtDesc(
                                conversation.getId(),
                                ChatMessageType.PRODUCT
                        )
                        .orElse(null);

        boolean sameProduct =
                lastProductContext != null
                        &&
                        lastProductContext.getProduct()
                                != null
                        &&
                        lastProductContext
                                .getProduct()
                                .getId()
                                .equals(productId);

        if (!sameProduct) {

            User customer =
                    conversation
                            .getCustomer();

            ChatMessage contextMessage =
                    new ChatMessage();

            contextMessage.setConversation(
                    conversation
            );

            contextMessage.setSender(
                    customer
            );

            contextMessage.setType(
                    ChatMessageType.PRODUCT
            );

            contextMessage.setProduct(
                    product
            );

            contextMessage.setContent(
                    "Sản phẩm đang trao đổi"
            );

            contextMessage.setReadStatus(
                    true
            );

            contextMessage.setReadAt(
                    LocalDateTime.now()
            );

            ChatMessage saved =
                    messageRepository
                            .save(
                                    contextMessage
                            );

            conversation.setUpdatedAt(
                    LocalDateTime.now()
            );

            publishMessageCreated(
                    conversation,
                    customerId,
                    toMessageResponse(saved)
            );
        }

        publishProductContextChanged(
                conversation,
                customerId
        );

        return toConversationResponse(
                conversation,
                customerId
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse>
    getMessages(
            Long conversationId
    ) {

        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        ChatConversation conversation =
                getAuthorizedConversation(
                        conversationId,
                        userId
                );

        return messageRepository
                .findByConversation_IdOrderByCreatedAtAsc(
                        conversation.getId()
                )
                .stream()
                .map(this::toMessageResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(
            Long conversationId,
            SendChatMessageRequest request
    ) {

        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        ChatConversation conversation =
                getAuthorizedConversation(
                        conversationId,
                        userId
                );

        User sender =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy người gửi"
                                        )
                        );

        ChatMessage message =
                new ChatMessage();

        message.setConversation(
                conversation
        );

        message.setSender(
                sender
        );

        message.setContent(
                request
                        .getContent()
                        .trim()
        );

        message.setType(
                ChatMessageType.TEXT
        );

        message.setReadStatus(false);

        ChatMessage saved =
                messageRepository
                        .save(message);

        LocalDateTime now =
                LocalDateTime.now();

        conversation.setUpdatedAt(now);
        conversation.setLastMessageAt(now);
        ChatMessageResponse response = toMessageResponse(saved);
        publishMessageCreated(
                conversation,
                userId,
                response
        );

        return response;
    }

    @Transactional
    public void markAsRead(
            Long conversationId
    ) {

        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        ChatConversation conversation =
                getAuthorizedConversation(
                        conversationId,
                        userId
                );

        List<ChatMessage> unreadMessages =
                messageRepository
                        .findByConversation_IdAndSender_IdNotAndReadStatusFalse(
                                conversation.getId(),
                                userId
                        );

        LocalDateTime now =
                LocalDateTime.now();

        for (
                ChatMessage message :
                unreadMessages
        ) {
            message.setReadStatus(true);
            message.setReadAt(now);
        }

        ChatRealtimeEventResponse event =
                new ChatRealtimeEventResponse(
                        "MESSAGES_READ",
                        conversation.getId(),
                        userId,
                        null
                );

        Long customerId =
                conversation
                        .getCustomer()
                        .getId();

        Long sellerId =
                conversation
                        .getShop()
                        .getOwner()
                        .getId();

        sendToUser(
                customerId,
                event
        );

        if (!sellerId.equals(customerId)) {
            sendToUser(
                    sellerId,
                    event
            );
        }
    }

    private ChatConversation
    getAuthorizedConversation(
            Long conversationId,
            Long userId
    ) {

        ChatConversation conversation =
                conversationRepository
                        .findById(
                                conversationId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Không tìm thấy cuộc trò chuyện"
                                        )
                        );

        boolean isCustomer =
                conversation
                        .getCustomer()
                        .getId()
                        .equals(userId);

        boolean isSeller =
                conversation
                        .getShop()
                        .getOwner()
                        .getId()
                        .equals(userId);

        if (!isCustomer && !isSeller) {
            throw new AccessDeniedException(
                    "Bạn không có quyền truy cập cuộc trò chuyện này"
            );
        }

        return conversation;
    }

    private ChatConversationResponse
    toConversationResponse(
            ChatConversation conversation,
            Long currentUserId
    ) {

        ChatConversationResponse response =
                new ChatConversationResponse();

        response.setId(
                conversation.getId()
        );

        response.setCustomerId(
                conversation
                        .getCustomer()
                        .getId()
        );

        response.setCustomerName(
                conversation
                        .getCustomer()
                        .getFullName()
        );

        response.setShopId(
                conversation
                        .getShop()
                        .getId()
        );

        response.setShopName(
                conversation
                        .getShop()
                        .getName()
        );

        response.setShopLogoUrl(
                conversation
                        .getShop()
                        .getLogoUrl()
        );

        messageRepository
                .findFirstByConversation_IdAndTypeOrderByCreatedAtDesc(
                        conversation.getId(),
                        ChatMessageType.TEXT
                )
                .ifPresent(lastMessage -> {

                    response.setLastMessage(
                            lastMessage
                                    .getContent()
                    );

                    response.setLastMessageAt(
                            lastMessage
                                    .getCreatedAt()
                    );
                });

        boolean productContextVisible =
                !Boolean.FALSE.equals(
                        conversation
                                .getProductContextVisible()
                );

        if (productContextVisible) {

            messageRepository
                    .findFirstByConversation_IdAndTypeOrderByCreatedAtDesc(
                            conversation.getId(),
                            ChatMessageType.PRODUCT
                    )
                    .filter(message ->
                            message.getProduct() != null
                    )
                    .ifPresent(message ->
                            response.setContextProduct(
                                    toChatProductResponse(
                                            message.getProduct()
                                    )
                            )
                    );
        }

        response.setUnreadCount(
                messageRepository
                        .countByConversation_IdAndSender_IdNotAndReadStatusFalse(
                                conversation.getId(),
                                currentUserId
                        )
        );

        return response;
    }

    @Transactional
    public void closeProductContext(
            Long conversationId
    ) {

        Long userId =
                SecurityUtils
                        .getCurrentUserId();

        ChatConversation conversation =
                getAuthorizedConversation(
                        conversationId,
                        userId
                );
        if (
                !conversation
                        .getCustomer()
                        .getId()
                        .equals(userId)
        ) {
            throw new AccessDeniedException(
                    "Chỉ khách hàng mới có thể đóng thông tin sản phẩm"
            );
        }

        conversation.setProductContextVisible(
                false
        );

        publishProductContextChanged(
                conversation,
                userId
        );
    }

    private ChatMessageResponse
    toMessageResponse(
            ChatMessage message
    ) {

        ChatMessageResponse response =
                new ChatMessageResponse();

        response.setId(
                message.getId()
        );

        response.setConversationId(
                message
                        .getConversation()
                        .getId()
        );

        response.setSenderId(
                message
                        .getSender()
                        .getId()
        );

        response.setSenderName(
                message
                        .getSender()
                        .getFullName()
        );

        ChatMessageType type =
                message.getType() != null
                        ? message.getType()
                        : ChatMessageType.TEXT;

        response.setType(type);

        if (
                type == ChatMessageType.PRODUCT
                        &&
                        message.getProduct() != null
        ) {
            response.setProduct(
                    toChatProductResponse(
                            message.getProduct()
                    )
            );
        }

        response.setContent(
                message.getContent()
        );

        response.setReadStatus(
                message.getReadStatus()
        );

        response.setCreatedAt(
                message.getCreatedAt()
        );

        response.setReadAt(
                message.getReadAt()
        );

        return response;
    }

    private ChatProductResponse
    toChatProductResponse(
            Product product
    ) {

        ChatProductResponse response =
                new ChatProductResponse();

        response.setId(
                product.getId()
        );

        response.setName(
                product.getName()
        );

        response.setPrice(
                product.getPrice()
        );

        response.setImageUrl(
                getPrimaryImageUrl(
                        product
                )
        );

        FlashSale flashSale =
                pricingService
                        .getActiveFlashSale(
                                product
                        );

        response.setSalePrice(
                flashSale != null
                        ? flashSale.getSalePrice()
                        : null
        );

        return response;
    }

    private String getPrimaryImageUrl(
            Product product
    ) {

        if (
                product == null
                        ||
                        product.getImages() == null
        ) {
            return null;
        }

        return product
                .getImages()
                .stream()
                .filter(image ->
                        Boolean.TRUE.equals(
                                image.getPrimaryImage()
                        )
                )
                .map(
                        ProductImage::getImageUrl
                )
                .findFirst()
                .orElseGet(() ->
                        product
                                .getImages()
                                .stream()
                                .map(
                                        ProductImage::getImageUrl
                                )
                                .filter(url ->
                                        url != null
                                                &&
                                                !url.isBlank()
                                )
                                .findFirst()
                                .orElse(null)
                );
    }

    private void publishMessageCreated(
            ChatConversation conversation,
            Long senderId,
            ChatMessageResponse message
    ) {

        ChatRealtimeEventResponse event =
                new ChatRealtimeEventResponse(
                        "MESSAGE_CREATED",
                        conversation.getId(),
                        senderId,
                        message
                );

        Long customerId =
                conversation
                        .getCustomer()
                        .getId();

        Long sellerId =
                conversation
                        .getShop()
                        .getOwner()
                        .getId();

        sendToUser(
                customerId,
                event
        );

        if (!sellerId.equals(customerId)) {

            sendToUser(
                    sellerId,
                    event
            );
        }
    }

    private void sendToUser(
            Long userId,
            ChatRealtimeEventResponse event
    ) {

        messagingTemplate
                .convertAndSendToUser(
                        String.valueOf(userId),
                        "/queue/shop-chat",
                        event
                );
    }

    private void publishProductContextChanged(
            ChatConversation conversation,
            Long actorUserId
    ) {

        ChatRealtimeEventResponse event =
                new ChatRealtimeEventResponse(
                        "PRODUCT_CONTEXT_CHANGED",
                        conversation.getId(),
                        actorUserId,
                        null
                );

        Long customerId =
                conversation
                        .getCustomer()
                        .getId();

        Long sellerId =
                conversation
                        .getShop()
                        .getOwner()
                        .getId();

        sendToUser(
                customerId,
                event
        );

        if (!sellerId.equals(customerId)) {
            sendToUser(
                    sellerId,
                    event
            );
        }
    }
}