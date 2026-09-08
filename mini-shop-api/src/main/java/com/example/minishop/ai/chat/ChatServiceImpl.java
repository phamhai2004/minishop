package com.example.minishop.ai.chat;

import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.ai.dto.ChatResponse;
import com.example.minishop.dto.response.ProductVariantOptionResponse;
import com.example.minishop.dto.response.ProductVariantResponse;
import com.example.minishop.service.ProductService;
import com.example.minishop.service.WishlistService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final RestClient restClient;
    private final ProductService productService;
    private final ChatQueryParser chatQueryParser;
    private final ChatIntentDetector chatIntentDetector;
    private final ChatContextStore chatContextStore;
    private final ProductReferenceParser productReferenceParser;
    private final WishlistService wishlistService;

    public ChatServiceImpl(
            ProductService productService,
            ChatQueryParser chatQueryParser,
            ChatIntentDetector chatIntentDetector,
            ChatContextStore chatContextStore,
            ProductReferenceParser productReferenceParser,
            WishlistService wishlistService
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("http://ai:8000")
                .build();
        this.productService = productService;
        this.chatQueryParser = chatQueryParser;
        this.chatIntentDetector = chatIntentDetector;
        this.chatContextStore = chatContextStore;
        this.productReferenceParser = productReferenceParser;
        this.wishlistService = wishlistService;
    }
    @Override
    public ChatResponse chat(Long userId, String message) {


        ChatIntent intent =
                chatIntentDetector.detect(message);

        if (intent == ChatIntent.GREETING) {

            chatContextStore.clear(
                    userId
            );

            chatContextStore.clearConversation(
                    userId
            );

            return new ChatResponse(
                    "Xin chào! 👋 Tôi là trợ lý của Hair. "
                            + "Tôi có thể giúp bạn tìm và tư vấn sản phẩm. "
                            + "Bạn đang quan tâm sản phẩm nào ạ?",
                    List.of()
            );
        }

        if (productReferenceParser
                .isPreviousProductReference(
                        message
                )) {

            ProductResponse selectedProduct =
                    chatContextStore
                            .getLastSelectedProduct(
                                    userId
                            );

            if (selectedProduct == null) {

                return new ChatResponse(
                        "Mình chưa xác định được bạn đang nhắc tới sản phẩm nào. "
                                + "Bạn có thể nói ví dụ: \"cái thứ 2\" nhé.",
                        List.of()
                );
            }

            return answerReferencedProduct(
                    message,
                    selectedProduct
            );
        }

        List<Integer> referencedIndexes =
                productReferenceParser
                        .detectMultipleIndexes(
                                message
                        );

        if (isComparisonQuestion(message)
                && referencedIndexes.size() >= 2) {

            return compareReferencedProducts(
                    userId,
                    message,
                    referencedIndexes
            );
        }

        Integer referencedIndex =
                productReferenceParser
                        .detectSingleIndex(
                                message
                        );

        if (referencedIndex != null) {

            List<ProductResponse> lastProducts =
                    chatContextStore
                            .getLastProducts(
                                    userId
                            );

            if (lastProducts.isEmpty()) {

                return new ChatResponse(
                        "Mình chưa có danh sách sản phẩm trước đó để tham chiếu. "
                                + "Bạn hãy tìm hoặc yêu cầu tư vấn sản phẩm trước nhé.",
                        List.of()
                );
            }

            if (referencedIndex < 0
                    || referencedIndex >= lastProducts.size()) {

                return new ChatResponse(
                        "Danh sách trước đó chỉ có "
                                + lastProducts.size()
                                + " sản phẩm. "
                                + "Bạn chọn lại số sản phẩm giúp mình nhé.",
                        lastProducts
                );
            }

            ProductResponse referencedProduct =
                    lastProducts.get(
                            referencedIndex
                    );

            chatContextStore.saveLastSelectedProduct(
                    userId,
                    referencedProduct
            );

            return answerReferencedProduct(
                    message,
                    referencedProduct
            );
        }

        if (isCheapestQuestion(message)) {

            return answerCheapestProduct(
                    userId
            );
        }

        if (isMostExpensiveQuestion(message)) {

            return answerMostExpensiveProduct(
                    userId
            );
        }

        if (intent == ChatIntent.TOP_SELLING_PRODUCTS) {

            List<ProductResponse> products =
                    productService
                            .getTopSellingProductsForChat();

            return chatTopProductsWithAI(
                    message,
                    intent,
                    products
            );
        }

        if (intent == ChatIntent.TOP_FAVORITE_PRODUCTS) {

            List<ProductResponse> products =
                    wishlistService
                            .getTopFavoriteProductsForChat();

            return chatTopProductsWithAI(
                    message,
                    intent,
                    products
            );
        }

        ChatQuery query = chatQueryParser.parse(message);

        PendingRecommendationContext pending =
                chatContextStore.get(
                        userId
                );

        boolean hasProductTarget =
                hasProductTarget(query);

        if (pending != null
                && hasProductTarget) {

            query =
                    mergePendingRecommendation(
                            query,
                            pending
                    );

            intent =
                    ChatIntent
                            .PRODUCT_RECOMMENDATION;

            chatContextStore.clear(
                    userId
            );
        }

        if (intent == ChatIntent.PRODUCT_RECOMMENDATION
                && !hasProductTarget(query)) {

            chatContextStore.save(
                    userId,
                    new PendingRecommendationContext(
                            query.minPrice(),
                            query.maxPrice(),
                            query.budgetTarget(),
                            query.purpose()
                    )
            );

            return new ChatResponse(
                    buildCategoryClarification(
                            query
                    ),
                    List.of()
            );
        }

        if (query.keyword() == null
                && query.categoryId() == null
                && query.minPrice() == null
                && query.maxPrice() == null
                && query.budgetTarget() == null
                && query.purpose() == null) {

            return chatWithAI(
                    message,
                    intent
            );
        }

        List<ProductResponse> products =
                findRelevantProducts(query);

        if (intent == ChatIntent.PRODUCT_PRICE) {

            if (products.isEmpty()) {
                return new ChatResponse(
                        "Tôi chưa tìm thấy sản phẩm phù hợp.",
                        List.of()
                );
            }

            return new ChatResponse(
                    answerProductPrice(products),
                    products
            );
        }

        if (intent == ChatIntent.PRODUCT_STOCK) {

            if (products.isEmpty()) {
                return new ChatResponse(
                        "Tôi chưa tìm thấy sản phẩm phù hợp.",
                        List.of()
                );
            }

            return new ChatResponse(
                    answerProductStock(products),
                    products
            );
        }

        return chatWithProducts(
                userId,
                message,
                intent,
                query,
                products
        );
    }

    private boolean isComparisonQuestion(
            String message
    ) {

        String normalized =
                normalizeMessage(
                        message
                );

        return normalized.contains(
                "so sanh"
        )
                || normalized.contains(
                "cai nao tot hon"
        )
                || normalized.contains(
                "con nao tot hon"
        )
                || normalized.contains(
                "cai nao hon"
        );
    }

    private ChatResponse compareReferencedProducts(
            Long userId,
            String message,
            List<Integer> indexes
    ) {

        List<ProductResponse> lastProducts =
                chatContextStore
                        .getLastProducts(
                                userId
                        );

        if (lastProducts.isEmpty()) {

            return new ChatResponse(
                    "Mình chưa có danh sách sản phẩm trước đó để so sánh. "
                            + "Bạn hãy yêu cầu tư vấn sản phẩm trước nhé.",
                    List.of()
            );
        }

        List<ProductResponse> selectedProducts =
                indexes.stream()
                        .filter(index ->
                                index >= 0
                                        &&
                                        index < lastProducts.size()
                        )
                        .distinct()
                        .limit(4)
                        .map(lastProducts::get)
                        .toList();

        if (selectedProducts.size() < 2) {

            return new ChatResponse(
                    "Mình chưa xác định được đủ hai sản phẩm để so sánh. "
                            + "Bạn có thể nói ví dụ: "
                            + "\"so sánh cái 1 và cái 2\".",
                    lastProducts
            );
        }

        return chatComparisonWithAI(
                message,
                selectedProducts
        );
    }

    private ChatResponse chatComparisonWithAI(
            String message,
            List<ProductResponse> products
    ) {

        StringBuilder context =
                new StringBuilder();

        context.append(
                "PRODUCT_FOUND = true\n"
        );

        context.append(
                "PRODUCT_COUNT = "
        );

        context.append(
                products.size()
        );

        context.append("\n\n");

        int index = 1;

        for (ProductResponse product : products) {

            appendProductContext(
                    context,
                    product,
                    index
            );

            index++;
        }

        Map<String, String> request =
                Map.of(
                        "message",
                        message,
                        "product_context",
                        context.toString(),
                        "intent",
                        "PRODUCT_COMPARISON"
                );

        try {

            com.example.minishop.ai.dto.ChatResponse response =
                    restClient
                            .post()
                            .uri(
                                    "/api/v1/chat"
                            )
                            .body(
                                    request
                            )
                            .retrieve()
                            .body(
                                    com.example.minishop.ai.dto.ChatResponse.class
                            );

            if (response == null
                    || response.message() == null
                    || response.message().isBlank()) {

                return fallbackProductResponse(
                        products
                );
            }

            return new ChatResponse(
                    response.message(),
                    products
            );

        } catch (Exception exception) {

            return fallbackProductResponse(
                    products
            );
        }
    }

    private ChatResponse answerReferencedProduct(
            String message,
            ProductResponse product
    ) {

        String normalized =
                normalizeMessage(
                        message
                );

        BigDecimal effectivePrice =
                getEffectivePrice(
                        product
                );

        if (normalized.contains("con hang")
                || normalized.contains("con bao nhieu")
                || normalized.contains("so luong")) {

            return new ChatResponse(
                    product.getName()
                            + " hiện còn "
                            + product.getQuantity()
                            + " sản phẩm.",
                    List.of(product)
            );
        }

        if (normalized.contains("gia")
                || normalized.contains("bao nhieu tien")) {

            return new ChatResponse(
                    product.getName()
                            + " hiện có giá "
                            + formatPrice(
                            effectivePrice
                    )
                            + " VNĐ.",
                    List.of(product)
            );
        }

        return new ChatResponse(
                "Bạn đang hỏi về "
                        + product.getName()
                        + ". Sản phẩm hiện có giá "
                        + formatPrice(
                        effectivePrice
                )
                        + " VNĐ và còn "
                        + product.getQuantity()
                        + " sản phẩm.",
                List.of(product)
        );
    }

    private String normalizeMessage(
            String message
    ) {

        if (message == null) {
            return "";
        }

        String value =
                message.toLowerCase(
                        Locale.ROOT
                );

        value =
                java.text.Normalizer.normalize(
                        value,
                        java.text.Normalizer.Form.NFD
                );

        value =
                value.replaceAll(
                        "\\p{M}+",
                        ""
                );

        value =
                value.replace(
                        'đ',
                        'd'
                );

        return value
                .replaceAll(
                        "[!?.,]+",
                        ""
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }

    private ChatQuery mergePendingRecommendation(
            ChatQuery current,
            PendingRecommendationContext pending
    ) {

        return new ChatQuery(
                current.keyword(),
                current.categoryId(),

                current.minPrice() != null
                        ? current.minPrice()
                        : pending.minPrice(),

                current.maxPrice() != null
                        ? current.maxPrice()
                        : pending.maxPrice(),

                current.budgetTarget() != null
                        ? current.budgetTarget()
                        : pending.budgetTarget(),

                current.purpose() != null
                        ? current.purpose()
                        : pending.purpose()
        );
    }

    private ChatResponse chatWithProducts(
            Long userId,
            String message,
            ChatIntent intent,
            ChatQuery query,
            List<ProductResponse> products
    ){

        List<ProductResponse> contextProducts =
                products == null
                        ? List.of()
                        : products.stream()
                        .limit(8)
                        .toList();

        if (!contextProducts.isEmpty()) {

            chatContextStore.saveLastProducts(
                    userId,
                    contextProducts
            );
        }

        String productContext =
                buildProductContext(
                        query,
                        contextProducts
                );

        Map<String, String> request =
                Map.of(
                        "message",
                        message,

                        "product_context",
                        productContext,

                        "intent",
                        intent.name()
                );

        try {

            com.example.minishop.ai.dto.ChatResponse aiResponse =
                    restClient
                            .post()
                            .uri("/api/v1/chat")
                            .body(request)
                            .retrieve()
                            .body(
                                    com.example.minishop.ai.dto.ChatResponse.class
                            );

            if (aiResponse == null
                    || aiResponse.message() == null
                    || aiResponse.message().isBlank()) {

                return fallbackProductResponse(
                        contextProducts
                );
            }

            return new ChatResponse(
                    aiResponse.message(),
                    contextProducts
            );

        } catch (Exception exception) {

            return fallbackProductResponse(
                    contextProducts
            );
        }
    }

    private boolean hasProductTarget(
            ChatQuery query
    ) {

        return query.keyword() != null
                || query.categoryId() != null;
    }

    private String buildCategoryClarification(
            ChatQuery query
    ) {

        StringBuilder answer =
                new StringBuilder(
                        "Bạn muốn mình tư vấn nhóm sản phẩm nào"
                );

        if (query.budgetTarget() != null) {

            answer.append(
                    " trong khoảng ngân sách "
            );

            answer.append(
                    formatPrice(
                            query.budgetTarget()
                    )
            );

            answer.append(" VNĐ");
        }

        if (query.purpose() != null) {

            answer.append(
                    " cho nhu cầu "
            );

            answer.append(
                    purposeLabel(
                            query.purpose()
                    )
            );
        }

        answer.append(
                "? Ví dụ: Laptop, điện thoại, máy ảnh..."
        );

        return answer.toString();
    }

    private String purposeLabel(
            String purpose
    ) {

        if (purpose == null) {
            return "";
        }

        return switch (purpose) {

            case "PROGRAMMING" ->
                    "học lập trình";

            case "GAMING" ->
                    "chơi game";

            case "GRAPHICS" ->
                    "đồ họa";

            case "OFFICE" ->
                    "văn phòng";

            case "STUDENT" ->
                    "học tập";

            default ->
                    purpose.toLowerCase();
        };
    }

    private String buildProductContext(
            ChatQuery query,
            List<ProductResponse> products
    ) {

        StringBuilder context =
                new StringBuilder();

        context.append(
                "USER_PURPOSE = "
        );

        context.append(
                query.purpose() != null
                        ? query.purpose()
                        : "NONE"
        );

        context.append("\n");

        context.append(
                "BUDGET_TARGET = "
        );

        context.append(
                query.budgetTarget() != null
                        ? query.budgetTarget()
                        : "NONE"
        );

        context.append("\n");

        context.append(
                "MIN_PRICE = "
        );

        context.append(
                query.minPrice() != null
                        ? query.minPrice()
                        : "NONE"
        );

        context.append("\n");

        context.append(
                "MAX_PRICE = "
        );

        context.append(
                query.maxPrice() != null
                        ? query.maxPrice()
                        : "NONE"
        );

        context.append("\n\n");

        if (products == null
                || products.isEmpty()) {

            context.append(
                    "PRODUCT_FOUND = false\n"
            );

            context.append(
                    "PRODUCT_COUNT = 0"
            );

            return context
                    .toString()
                    .trim();
        }

        context.append(
                "PRODUCT_FOUND = true\n"
        );

        context.append(
                "PRODUCT_COUNT = "
        );

        context.append(
                products.size()
        );

        context.append("\n\n");

        int index = 1;

        for (ProductResponse product : products) {

            appendProductContext(
                    context,
                    product,
                    index
            );

            index++;
        }

        return context
                .toString()
                .trim();
    }

    private String safeText(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return "Không có thông tin";
        }

        return value.trim();
    }

    private ChatResponse fallbackProductResponse(
            List<ProductResponse> products
    ) {

        if (products == null
                || products.isEmpty()) {

            return new ChatResponse(
                    "Tôi chưa tìm thấy sản phẩm phù hợp với yêu cầu của bạn.",
                    List.of()
            );
        }

        return new ChatResponse(
                answerProductSearch(
                        products
                ),
                products
        );
    }

    private ChatResponse chatWithAI(
            String message,
            ChatIntent intent
    ) {

        Map<String, String> request =
                Map.of(
                        "message",
                        message,

                        "product_context",
                        "PRODUCT_FOUND = false",

                        "intent",
                        intent.name()
                );

        try {

            ChatResponse response =
                    restClient
                            .post()
                            .uri("/api/v1/chat")
                            .body(request)
                            .retrieve()
                            .body(
                                    ChatResponse.class
                            );

            if (response == null
                    || response.message() == null
                    || response.message().isBlank()) {

                return new ChatResponse(
                        "Tôi chưa thể trả lời câu hỏi này lúc này.",
                        List.of()
                );
            }

            return new ChatResponse(
                    response.message(),
                    List.of()
            );

        } catch (Exception exception) {

            return new ChatResponse(
                    "Tôi đang gặp chút sự cố khi xử lý câu hỏi. Bạn thử lại sau nhé.",
                    List.of()
            );
        }
    }

    private String answerProductPrice(List<ProductResponse> products) {

        if (products == null || products.isEmpty()) {
            return "Tôi chưa tìm thấy sản phẩm phù hợp.";
        }

        if (products.size() == 1) {
            ProductResponse product = products.get(0);

            return product.getName()
                    + " hiện có giá "
                    + formatPrice(
                    getEffectivePrice(product))
                    + " VNĐ.";
        }

        StringBuilder answer = new StringBuilder();

        answer.append("Các sản phẩm phù hợp có giá:\n");

        for (ProductResponse product : products) {
            answer.append("- ")
                    .append(product.getName())
                    .append(": ")
                    .append(formatPrice(getEffectivePrice(product)))
                    .append(" VNĐ\n");
        }

        return answer.toString().trim();
    }

    private String answerProductStock(List<ProductResponse> products) {

        if (products == null || products.isEmpty()) {
            return "Tôi chưa tìm thấy sản phẩm phù hợp.";
        }

        if (products.size() == 1) {
            ProductResponse product = products.get(0);

            return String.format(
                    "%s hiện còn %,d sản phẩm.",
                    product.getName(),
                    product.getQuantity()
            );
        }

        StringBuilder answer = new StringBuilder();

        answer.append("Số lượng sản phẩm hiện có:\n");

        for (ProductResponse product : products) {
            answer.append("- ")
                    .append(product.getName())
                    .append(": ")
                    .append(String.format("%,d", product.getQuantity()))
                    .append(" sản phẩm\n");
        }

        return answer.toString().trim();
    }

    private String answerProductSearch(List<ProductResponse> products) {

        if (products == null || products.isEmpty()) {
            return "Tôi chưa tìm thấy sản phẩm phù hợp với yêu cầu của bạn.";
        }

        StringBuilder answer = new StringBuilder();

        answer.append("Tôi tìm thấy ")
                .append(products.size())
                .append(" sản phẩm phù hợp:\n\n");

        for (ProductResponse product : products) {

            answer.append("- ")
                    .append(product.getName())
                    .append(" | Giá: ")
                    .append(formatPrice(getEffectivePrice(product)))
                    .append(" VNĐ")
                    .append(" | Còn: ")
                    .append(product.getQuantity())
                    .append(" sản phẩm")
                    .append("\n");
        }

        return answer.toString().trim();
    }

    private List<ProductResponse> findRelevantProducts(ChatQuery query) {

        if (query.keyword() != null
                || query.categoryId() != null
                || query.minPrice() != null
                || query.maxPrice() != null) {

            return productService.searchProductsForChat(
                    query.keyword(),
                    query.categoryId(),
                    query.minPrice(),
                    query.maxPrice()
            );
        }

        return List.of();
    }


    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Chưa có thông tin giá";
        }
        return NumberFormat
                .getInstance(new Locale("vi", "VN"))
                .format(price);
    }

    private boolean isCheapestQuestion(
            String message
    ) {

        String normalized =
                normalizeMessage(
                        message
                );

        return normalized.contains(
                "re nhat"
        )
                || normalized.contains(
                "gia thap nhat"
        );
    }

    private boolean isMostExpensiveQuestion(
            String message
    ) {

        String normalized =
                normalizeMessage(
                        message
                );

        return normalized.contains(
                "dat nhat"
        )
                || normalized.contains(
                "gia cao nhat"
        );
    }

    private ChatResponse answerCheapestProduct(
            Long userId
    ) {

        List<ProductResponse> lastProducts =
                chatContextStore
                        .getLastProducts(
                                userId
                        );

        if (lastProducts.isEmpty()) {

            return new ChatResponse(
                    "Mình chưa có danh sách sản phẩm trước đó để so sánh giá.",
                    List.of()
            );
        }

        ProductResponse cheapest =
                lastProducts.stream()
                        .filter(product ->
                                getEffectivePrice(
                                        product
                                ) != null
                        )
                        .min(
                                java.util.Comparator
                                        .comparing(
                                                this::getEffectivePrice
                                        )
                        )
                        .orElse(null);

        if (cheapest == null) {

            return new ChatResponse(
                    "Mình chưa có đủ thông tin giá để xác định sản phẩm rẻ nhất.",
                    lastProducts
            );
        }

        chatContextStore
                .saveLastSelectedProduct(
                        userId,
                        cheapest
                );

        return new ChatResponse(
                cheapest.getName()
                        + " là sản phẩm có giá thấp nhất trong danh sách trước đó, "
                        + "với giá "
                        + formatPrice(
                        getEffectivePrice(
                                cheapest
                        )
                )
                        + " VNĐ.",
                List.of(
                        cheapest
                )
        );
    }

    private ChatResponse answerMostExpensiveProduct(
            Long userId
    ) {

        List<ProductResponse> lastProducts =
                chatContextStore
                        .getLastProducts(
                                userId
                        );

        if (lastProducts.isEmpty()) {

            return new ChatResponse(
                    "Mình chưa có danh sách sản phẩm trước đó để so sánh giá.",
                    List.of()
            );
        }

        ProductResponse mostExpensive =
                lastProducts.stream()
                        .filter(product ->
                                getEffectivePrice(
                                        product
                                ) != null
                        )
                        .max(
                                java.util.Comparator
                                        .comparing(
                                                this::getEffectivePrice
                                        )
                        )
                        .orElse(null);

        if (mostExpensive == null) {

            return new ChatResponse(
                    "Mình chưa có đủ thông tin giá để xác định sản phẩm đắt nhất.",
                    lastProducts
            );
        }

        chatContextStore
                .saveLastSelectedProduct(
                        userId,
                        mostExpensive
                );

        return new ChatResponse(
                mostExpensive.getName()
                        + " là sản phẩm có giá cao nhất trong danh sách trước đó, "
                        + "với giá "
                        + formatPrice(
                        getEffectivePrice(
                                mostExpensive
                        )
                )
                        + " VNĐ.",
                List.of(
                        mostExpensive
                )
        );
    }

    private void appendProductContext(
            StringBuilder context,
            ProductResponse product,
            int index
    ) {

        context.append("PRODUCT_")
                .append(index)
                .append(":\n");

        context.append("ID: ")
                .append(product.getId() != null
                        ? product.getId()
                        : "NONE")
                .append("\n");

        context.append("Tên: ")
                .append(safeText(product.getName()))
                .append("\n");

        context.append("Danh mục: ")
                .append(safeText(product.getCategoryName()))
                .append("\n");

        context.append("Mô tả: ")
                .append(safeText(product.getDescription()))
                .append("\n");

        context.append("Giá gốc: ");

        if (product.getPrice() != null) {
            context.append(
                    formatPrice(product.getPrice())
            ).append(" VNĐ");
        } else {
            context.append("Chưa có thông tin");
        }

        context.append("\n");

        boolean hasFlashSale =
                Boolean.TRUE.equals(
                        product.getFlashSale()
                )
                        && product.getSalePrice() != null;

        context.append("Flash Sale: ")
                .append(
                        hasFlashSale
                                ? "true"
                                : "false"
                )
                .append("\n");

        context.append("Giá hiện tại: ");

        BigDecimal currentPrice =
                hasFlashSale
                        ? product.getSalePrice()
                        : product.getPrice();

        if (currentPrice != null) {
            context.append(
                    formatPrice(currentPrice)
            ).append(" VNĐ");
        } else {
            context.append("Chưa có thông tin");
        }

        context.append("\n");

        if (hasFlashSale) {

            context.append("Số lượng Flash Sale: ")
                    .append(
                            product.getFlashSaleQuantity() != null
                                    ? product.getFlashSaleQuantity()
                                    : 0
                    )
                    .append("\n");

            context.append("Đã bán Flash Sale: ")
                    .append(
                            product.getFlashSaleSold() != null
                                    ? product.getFlashSaleSold()
                                    : 0
                    )
                    .append("\n");

            context.append("Flash Sale còn lại: ")
                    .append(
                            product.getRemain() != null
                                    ? product.getRemain()
                                    : 0
                    )
                    .append("\n");
        }

        context.append("Tổng tồn kho: ")
                .append(
                        product.getQuantity() != null
                                ? product.getQuantity()
                                : 0
                )
                .append("\n");

        context.append("Shop: ")
                .append(
                        safeText(
                                product.getShopName()
                        )
                )
                .append("\n");

        context.append("Trạng thái: ")
                .append(
                        safeText(
                                product.getStatusName()
                        )
                )
                .append("\n");

        appendVariantContext(
                context,
                product.getVariants()
        );

        context.append("\n");
    }

    private void appendVariantContext(
            StringBuilder context,
            List<ProductVariantResponse> variants
    ) {

        if (variants == null
                || variants.isEmpty()) {

            context.append(
                    "Biến thể: Không có\n"
            );

            return;
        }

        context.append(
                "Biến thể:\n"
        );

        int variantIndex = 1;

        for (
                ProductVariantResponse variant
                : variants
        ) {

            context.append("  VARIANT_")
                    .append(variantIndex)
                    .append(":\n");

            context.append("    SKU: ")
                    .append(
                            safeText(
                                    variant.getSku()
                            )
                    )
                    .append("\n");

            context.append("    Giá: ");

            if (variant.getPrice() != null) {

                context.append(
                        formatPrice(
                                variant.getPrice()
                        )
                ).append(" VNĐ");

            } else {

                context.append(
                        "Chưa có thông tin"
                );
            }

            context.append("\n");

            context.append("    Tồn kho: ")
                    .append(
                            variant.getQuantity() != null
                                    ? variant.getQuantity()
                                    : 0
                    )
                    .append("\n");

            appendVariantOptions(
                    context,
                    variant.getOptions()
            );

            variantIndex++;
        }
    }

    private void appendVariantOptions(
            StringBuilder context,
            List<ProductVariantOptionResponse> options
    ) {

        if (options == null
                || options.isEmpty()) {

            context.append(
                    "    Tùy chọn: Không có\n"
            );

            return;
        }

        context.append(
                "    Tùy chọn:\n"
        );

        for (
                ProductVariantOptionResponse option
                : options
        ) {

            context.append("      - ")
                    .append(
                            safeText(
                                    option.getOptionTypeName()
                            )
                    )
                    .append(": ")
                    .append(
                            safeText(
                                    option.getOptionValueName()
                            )
                    )
                    .append("\n");
        }
    }

    private BigDecimal getEffectivePrice(
            ProductResponse product
    ) {

        if (product == null) {
            return null;
        }

        if (Boolean.TRUE.equals(
                product.getFlashSale()
        )
                && product.getSalePrice() != null) {

            return product.getSalePrice();
        }

        return product.getPrice();
    }

    private ChatResponse chatTopProductsWithAI(
            String message,
            ChatIntent intent,
            List<ProductResponse> products
    ) {

        List<ProductResponse> topProducts =
                products == null
                        ? List.of()
                        : products.stream()
                        .limit(5)
                        .toList();

        if (topProducts.isEmpty()) {

            return new ChatResponse(
                    "Hair hiện chưa có đủ dữ liệu để xếp hạng sản phẩm.",
                    List.of()
            );
        }

        ChatQuery emptyQuery =
                new ChatQuery(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        String productContext =
                buildProductContext(
                        emptyQuery,
                        topProducts
                );

        Map<String, String> request =
                Map.of(
                        "message",
                        message,
                        "product_context",
                        productContext,
                        "intent",
                        intent.name()
                );

        try {

            com.example.minishop.ai.dto.ChatResponse aiResponse =
                    restClient
                            .post()
                            .uri("/api/v1/chat")
                            .body(request)
                            .retrieve()
                            .body(
                                    com.example.minishop.ai.dto.ChatResponse.class
                            );

            if (aiResponse == null
                    || aiResponse.message() == null
                    || aiResponse.message().isBlank()) {

                return fallbackProductResponse(
                        topProducts
                );
            }

            return new ChatResponse(
                    aiResponse.message(),
                    topProducts
            );

        } catch (Exception exception) {

            return fallbackProductResponse(
                    topProducts
            );
        }
    }
}