package com.example.minishop.service;

import com.example.minishop.repository.ShopOrderRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ShopOrderCodeGenerator {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final String CHARACTERS =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyMMdd");

    private final ShopOrderRepository shopOrderRepository;

    public ShopOrderCodeGenerator(
            ShopOrderRepository shopOrderRepository
    ) {
        this.shopOrderRepository =
                shopOrderRepository;
    }

    public String generate() {

        for (int attempt = 0;
             attempt < 20;
             attempt++) {

            String code =
                    LocalDate.now().format(DATE_FORMAT)
                            + randomPart(8);

            if (!shopOrderRepository
                    .existsByOrderCode(code)) {

                return code;
            }
        }

        throw new IllegalStateException(
                "Không thể tạo mã đơn hàng duy nhất"
        );
    }

    private String randomPart(int length) {

        StringBuilder builder =
                new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index =
                    RANDOM.nextInt(
                            CHARACTERS.length()
                    );

            builder.append(
                    CHARACTERS.charAt(index)
            );
        }

        return builder.toString();
    }
}