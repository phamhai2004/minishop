package com.example.minishop.ai.chat;

import com.example.minishop.dto.response.CategoryResponse;
import com.example.minishop.dto.response.ProductResponse;
import com.example.minishop.service.CategoryService;
import com.example.minishop.service.ProductService;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatQueryParser {

    private final CategoryService categoryService;
    private final ProductService productService;
    private static final
    Map<String, String>
            CATEGORY_ALIASES =
            new LinkedHashMap<>();

    static {

        CATEGORY_ALIASES.put(
                "may tinh xach tay",
                "laptop"
        );

        CATEGORY_ALIASES.put(
                "may tinh",
                "laptop"
        );

        CATEGORY_ALIASES.put(
                "notebook",
                "laptop"
        );

        CATEGORY_ALIASES.put(
                "laptop",
                "laptop"
        );

        CATEGORY_ALIASES.put(
                "dien thoai",
                "dien thoai"
        );

        CATEGORY_ALIASES.put(
                "smartphone",
                "dien thoai"
        );

        CATEGORY_ALIASES.put(
                "phone",
                "dien thoai"
        );

        CATEGORY_ALIASES.put(
                "may anh",
                "may anh"
        );

        CATEGORY_ALIASES.put(
                "camera",
                "may anh"
        );

        /*
         * GIÀY DÉP
         */
        CATEGORY_ALIASES.put(
                "sneaker",
                "giay dep"
        );

        CATEGORY_ALIASES.put(
                "giay",
                "giay dep"
        );

        CATEGORY_ALIASES.put(
                "giay dep",
                "giay dep"
        );

        CATEGORY_ALIASES.put(
                "quan ao",
                "quan ao"
        );

        CATEGORY_ALIASES.put(
                "thoi trang",
                "quan ao"
        );
    }

    public ChatQueryParser(
            CategoryService categoryService,
            ProductService productService
    ) {
        this.categoryService =
                categoryService;

        this.productService =
                productService;
    }

    public ChatQuery parse(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return new ChatQuery(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        String normalized =
                normalize(message);

        CategoryResponse category =
                detectCategory(
                        normalized
                );

        BigDecimal minPrice = null;
        BigDecimal maxPrice = null;

        BigDecimal budgetTarget =
                extractBudgetTarget(
                        normalized
                );

        String purpose =
                detectPurpose(
                        normalized
                );

        BigDecimal[] priceRange =
                extractPriceRange(
                        normalized
                );

        if (priceRange != null) {

            minPrice =
                    priceRange[0];

            maxPrice =
                    priceRange[1];

        } else if (
                budgetTarget != null
        ) {

            minPrice =
                    budgetTarget.multiply(
                            new BigDecimal(
                                    "0.80"
                            )
                    );

            maxPrice =
                    budgetTarget.multiply(
                            new BigDecimal(
                                    "1.20"
                            )
                    );

        } else {

            minPrice =
                    extractMinPrice(
                            normalized
                    );

            maxPrice =
                    extractMaxPrice(
                            normalized
                    );
        }

        String keyword =
                detectProductKeyword(
                        normalized,
                        category
                );

        return new ChatQuery(
                keyword,
                category != null
                        ? category.getId()
                        : null,
                minPrice,
                maxPrice,
                budgetTarget,
                purpose
        );
    }

    private CategoryResponse detectCategory(
            String message
    ) {

        List<CategoryResponse> categories =
                categoryService.getAll();

        CategoryResponse directMatch =
                findCategoryByMessage(
                        message,
                        categories
                );

        if (directMatch != null) {
            return directMatch;
        }

        for (
                Map.Entry<String, String>
                        entry
                : CATEGORY_ALIASES
                .entrySet()
        ) {

            String alias =
                    entry.getKey();

            if (!message.contains(alias)) {
                continue;
            }

            String target =
                    entry.getValue();

            CategoryResponse category =
                    findCategoryByTarget(
                            target,
                            categories
                    );

            if (category != null) {
                return category;
            }
        }

        return null;
    }

    private CategoryResponse
    findCategoryByMessage(
            String message,
            List<CategoryResponse> categories
    ) {

        CategoryResponse bestMatch =
                null;

        int longest = -1;

        for (
                CategoryResponse category
                : categories
        ) {

            if (category.getName()
                    == null
                    ||
                    category.getName()
                            .isBlank()) {
                continue;
            }

            String categoryName =
                    normalize(
                            category.getName()
                    );

            if (
                    message.contains(
                            categoryName
                    )
                            &&
                            categoryName.length()
                                    > longest
            ) {

                bestMatch =
                        category;

                longest =
                        categoryName.length();
            }
        }

        return bestMatch;
    }

    private CategoryResponse
    findCategoryByTarget(
            String target,
            List<CategoryResponse> categories
    ) {

        String normalizedTarget =
                normalize(target);

        for (
                CategoryResponse category
                : categories
        ) {

            if (category.getName()
                    == null
                    ||
                    category.getName()
                            .isBlank()) {
                continue;
            }

            String categoryName =
                    normalize(
                            category.getName()
                    );

            if (
                    categoryName.equals(
                            normalizedTarget
                    )
                            ||
                            categoryName.contains(
                                    normalizedTarget
                            )
                            ||
                            normalizedTarget.contains(
                                    categoryName
                            )
            ) {
                return category;
            }
        }

        return null;
    }

    private String detectProductKeyword(
            String message,
            CategoryResponse category
    ) {

        Pattern existsPattern =
                Pattern.compile(
                        "^co\\s+(.+?)\\s+khong(?:\\s+a)?$",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher existsMatcher =
                existsPattern.matcher(
                        message
                );

        if (existsMatcher.matches()) {

            String candidate =
                    existsMatcher
                            .group(1)
                            .trim();

            if (candidate.endsWith(" nao")) {
                return null;
            }

            if (category != null
                    && isCategoryOnlyKeyword(
                    candidate,
                    category
            )) {

                return null;
            }

            return candidate;
        }

        List<ProductResponse> products =
                productService
                        .getActiveProductsForChat();

        ProductResponse bestMatch =
                null;

        String bestProductName =
                null;

        for (ProductResponse product : products) {

            if (product.getName() == null
                    || product.getName().isBlank()) {
                continue;
            }

            String productName =
                    normalize(
                            product.getName()
                    );

            if (message.contains(productName)) {

                if (bestProductName == null
                        || productName.length()
                        > bestProductName.length()) {

                    bestMatch =
                            product;

                    bestProductName =
                            productName;
                }
            }
        }

        if (bestMatch != null) {
            return bestProductName;
        }

        return null;
    }

    private boolean isCategoryOnlyKeyword(
            String candidate,
            CategoryResponse category
    ) {

        if (candidate == null
                || category == null
                || category.getName() == null) {

            return false;
        }

        String normalizedCandidate =
                normalize(
                        candidate
                );

        String normalizedCategoryName =
                normalize(
                        category.getName()
                );

        if (normalizedCandidate.equals(
                normalizedCategoryName
        )) {
            return true;
        }

        for (
                Map.Entry<String, String> entry
                : CATEGORY_ALIASES.entrySet()
        ) {

            String alias =
                    normalize(
                            entry.getKey()
                    );

            String target =
                    normalize(
                            entry.getValue()
                    );

            if (normalizedCandidate.equals(alias)
                    && normalizedCategoryName.contains(target)) {

                return true;
            }
        }

        return false;
    }

    private BigDecimal extractMinPrice(
            String message
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?:tren|tu|cao hon|lon hon|it nhat|toi thieu)"
                                + "\\s*([0-9]+(?:[.,][0-9]+)?)"
                                + "\\s*(trieu|tr|nghin|k|d|vnd)?",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        message
                );

        if (!matcher.find()) {
            return null;
        }

        return parsePrice(
                matcher.group(1),
                matcher.group(2)
        );
    }

    private BigDecimal[] extractPriceRange(
            String message
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?:tu)\\s*"
                                + "([0-9]+(?:[.,][0-9]+)?)"
                                + "\\s*(trieu|tr|k|nghin)?"
                                + "\\s*(?:den|toi|-)"
                                + "\\s*([0-9]+(?:[.,][0-9]+)?)"
                                + "\\s*(trieu|tr|k|nghin)?",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        message
                );

        if (!matcher.find()) {
            return null;
        }

        String minNumber =
                matcher.group(1);

        String minUnit =
                matcher.group(2);

        String maxNumber =
                matcher.group(3);

        String maxUnit =
                matcher.group(4);

        /*
         * "từ 15 đến 20 triệu"
         *
         * unit đầu null nhưng unit sau
         * là triệu → hiểu cả 2 đều triệu.
         */
        if (minUnit == null
                && maxUnit != null) {

            minUnit = maxUnit;
        }

        if (maxUnit == null
                && minUnit != null) {

            maxUnit = minUnit;
        }

        BigDecimal min =
                parsePrice(
                        minNumber,
                        minUnit
                );

        BigDecimal max =
                parsePrice(
                        maxNumber,
                        maxUnit
                );

        if (min.compareTo(max) > 0) {

            BigDecimal temp =
                    min;

            min = max;

            max = temp;
        }

        return new BigDecimal[]{
                min,
                max
        };
    }

    private BigDecimal extractBudgetTarget(
            String message
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?:khoang|khoang gia|tam|tam gia|gia tam|tam khoang|quanh)"
                                + "\\s*([0-9]+(?:[.,][0-9]+)?)"
                                + "\\s*(trieu|tr|k|nghin)?",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        message
                );

        if (!matcher.find()) {
            return null;
        }

        return parsePrice(
                matcher.group(1),
                matcher.group(2)
        );
    }
    private String detectPurpose(
            String message
    ) {

        if (message.contains("lap trinh")
                || message.contains("code")
                || message.contains("developer")) {

            return "PROGRAMMING";
        }

        if (message.contains("choi game")
                || message.contains("gaming")
                || message.contains("game")) {

            return "GAMING";
        }

        if (message.contains("do hoa")
                || message.contains("thiet ke")
                || message.contains("photoshop")
                || message.contains("video")) {

            return "GRAPHICS";
        }

        if (message.contains("van phong")
                || message.contains("office")) {

            return "OFFICE";
        }

        if (message.contains("sinh vien")
                || message.contains("hoc tap")) {

            return "STUDENT";
        }

        return null;
    }

    private BigDecimal extractMaxPrice(
            String message
    ) {

        Pattern pattern =
                Pattern.compile(
                        "(?:duoi|den|toi|thap hon|nho hon|it hon|toi da)"
                                + "\\s*([0-9]+(?:[.,][0-9]+)?)"
                                + "\\s*(trieu|tr|nghin|k|d|vnd)?",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        message
                );

        if (!matcher.find()) {
            return null;
        }

        return parsePrice(
                matcher.group(1),
                matcher.group(2)
        );
    }

    private BigDecimal parsePrice(
            String number,
            String unit
    ) {

        BigDecimal value =
                new BigDecimal(
                        number.replace(
                                ",",
                                "."
                        )
                );

        if (unit == null) {
            return value;
        }

        return switch (
                unit.toLowerCase(
                        Locale.ROOT
                )
                ) {

            case "trieu", "tr" ->
                    value.multiply(
                            BigDecimal.valueOf(
                                    1_000_000
                            )
                    );

            case "nghin", "k" ->
                    value.multiply(
                            BigDecimal.valueOf(
                                    1_000
                            )
                    );

            default ->
                    value;
        };
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
}