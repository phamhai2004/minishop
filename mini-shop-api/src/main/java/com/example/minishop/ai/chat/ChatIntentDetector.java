package com.example.minishop.ai.chat;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class ChatIntentDetector {

    public ChatIntent detect(String message) {

        if (message == null
                || message.isBlank()) {
            return ChatIntent.GENERAL_CHAT;
        }

        String normalized =
                normalize(message);

        if (isGreeting(normalized)) {
            return ChatIntent.GREETING;
        }

        if (isStockQuestion(normalized)) {
            return ChatIntent.PRODUCT_STOCK;
        }

        if (isPriceQuestion(normalized)) {
            return ChatIntent.PRODUCT_PRICE;
        }

        if (normalized.contains("ban chay nhat")
                || normalized.contains("ban chay")
                || normalized.contains("top san pham ban chay")) {

            return ChatIntent.TOP_SELLING_PRODUCTS;
        }

        if (normalized.contains("duoc yeu thich nhat")
                || normalized.contains("yeu thich nhat")
                || normalized.contains("top san pham duoc yeu thich")) {

            return ChatIntent.TOP_FAVORITE_PRODUCTS;
        }

        if (isRecommendationQuestion(
                normalized
        )) {
            return ChatIntent
                    .PRODUCT_RECOMMENDATION;
        }

        if (isProductSearchQuestion(
                normalized
        )) {
            return ChatIntent.PRODUCT_SEARCH;
        }

        return ChatIntent.GENERAL_CHAT;
    }

    private boolean isGreeting(
            String message
    ) {

        return message.equals("xin chao")
                || message.equals("chao")
                || message.equals("chao ban")
                || message.equals("hello")
                || message.equals("hi")
                || message.equals("hey");
    }

    private boolean isPriceQuestion(
            String message
    ) {

        return message.contains(
                "gia bao nhieu"
        )
                || message.contains(
                "bao nhieu tien"
        )
                || message.contains(
                "gia the nao"
        )
                || message.contains(
                "gia la bao nhieu"
        );
    }

    private boolean isStockQuestion(
            String message
    ) {

        return message.contains(
                "con bao nhieu"
        )
                || message.contains(
                "con hang khong"
        )
                || message.contains(
                "con hang"
        )
                || message.contains(
                "so luong"
        )
                || message.contains(
                "ton kho"
        )
                || message.contains(
                "con lai bao nhieu"
        );
    }

    private boolean isRecommendationQuestion(
            String message
    ) {

        return message.contains("tu van")
                || message.contains("goi y")
                || message.contains("de xuat")
                || message.contains("nen mua")
                || message.contains("nen chon")
                || message.contains("phu hop")
                || message.contains("recommend")
                || message.contains(
                "giup toi chon"
        )
                || message.contains(
                "giup minh chon"
        );
    }

    private boolean isProductSearchQuestion(
            String message
    ) {

        return (
                message.startsWith("co ")
                        &&
                        message.contains(" khong")
        )
                || message.contains(
                "shop co ban"
        )
                || message.contains(
                "co san pham"
        )
                || message.contains(
                "tim san pham"
        )
                || message.contains(
                "tim cho toi"
        )
                || message.contains(
                "tim giup toi"
        )
                || message.contains(
                "cho toi xem"
        )
                || message.contains(
                "san pham nao"
        )
                || message.contains(
                "loai nao"
        )
                || message.contains(
                "mat hang nao"
        )
                || message.contains(
                "toi muon mua"
        )
                || message.contains(
                "minh muon mua"
        );
    }

    private String normalize(
            String message
    ) {

        String value =
                message.toLowerCase(
                        Locale.ROOT
                );
        
        value =
                Normalizer.normalize(
                        value,
                        Normalizer.Form.NFD
                );

        value =
                value.replaceAll(
                        "\\p{M}+",
                        ""
                );

        value =
                value.replace('đ', 'd');

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
}