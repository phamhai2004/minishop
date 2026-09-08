package com.example.minishop.ai.chat;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProductReferenceParser {

    public Integer detectSingleIndex(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return null;
        }

        String normalized =
                normalize(message);

        /*
         * cái thứ 2
         * con thứ 2
         * sản phẩm thứ 2
         * sản phẩm số 2
         */
        Pattern numericPattern =
                Pattern.compile(
                        "(?:cai|con|san pham)?\\s*"
                                + "(?:thu|so)\\s*"
                                + "(\\d+)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher numericMatcher =
                numericPattern.matcher(
                        normalized
                );

        if (numericMatcher.find()) {

            int index =
                    Integer.parseInt(
                            numericMatcher.group(1)
                    );

            return index > 0
                    ? index - 1
                    : null;
        }

        if (normalized.contains("cai dau")
                || normalized.contains("con dau")
                || normalized.contains("dau tien")) {

            return 0;
        }

        if (normalized.contains("cai thu hai")
                || normalized.contains("con thu hai")) {

            return 1;
        }

        if (normalized.contains("cai thu ba")
                || normalized.contains("con thu ba")) {

            return 2;
        }

        return null;
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

    public List<Integer> detectMultipleIndexes(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return List.of();
        }

        String normalized =
                normalize(message);

        List<Integer> indexes =
                new ArrayList<>();

        Pattern pattern =
                Pattern.compile(
                        "(?:cai|con|san pham)?\\s*"
                                + "(?:thu|so)?\\s*"
                                + "(\\d+)",
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(
                        normalized
                );

        while (matcher.find()) {

            int number =
                    Integer.parseInt(
                            matcher.group(1)
                    );

            if (number <= 0) {
                continue;
            }

            int index =
                    number - 1;

            if (!indexes.contains(index)) {
                indexes.add(index);
            }
        }

        return indexes;
    }

    public boolean isPreviousProductReference(
            String message
    ) {

        if (message == null
                || message.isBlank()) {

            return false;
        }

        String normalized =
                normalize(message);

        return normalized.matches(
                ".*\\b(no|cai do|con do|san pham do|cai nay|con nay)\\b.*"
        );
    }
}