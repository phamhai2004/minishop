package com.example.minishop.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class VnPayUtil {

    private static final String HMAC_SHA_512 = "HmacSHA512";

    private VnPayUtil() {
    }

    public static String hmacSha512(
            String secretKey,
            String data
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException(
                    "VNPay hash secret không được để trống"
            );
        }

        if (data == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu ký VNPay không được null"
            );
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA_512);

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            secretKey.getBytes(StandardCharsets.UTF_8),
                            HMAC_SHA_512
                    );

            mac.init(secretKeySpec);

            byte[] hashBytes = mac.doFinal(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            return bytesToHex(hashBytes);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Không thể tạo chữ ký VNPay",
                    exception
            );
        }
    }

    public static String buildPaymentUrl(
            String payUrl,
            String hashSecret,
            Map<String, String> parameters
    ) {
        if (payUrl == null || payUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "VNPay pay URL không được để trống"
            );
        }

        Map<String, String> sortedParams =
                new TreeMap<>(parameters);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (Map.Entry<String, String> entry
                : sortedParams.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.isBlank()) {
                continue;
            }

            if (!hashData.isEmpty()) {
                hashData.append('&');
                query.append('&');
            }

            String encodedKey = encode(key);
            String encodedValue = encode(value);

            hashData
                    .append(encodedKey)
                    .append('=')
                    .append(encodedValue);

            query
                    .append(encodedKey)
                    .append('=')
                    .append(encodedValue);
        }

        String secureHash = hmacSha512(
                hashSecret,
                hashData.toString()
        );

        return payUrl
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;
    }

    public static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result =
                new StringBuilder(bytes.length * 2);

        for (byte value : bytes) {
            result.append(
                    String.format("%02x", value)
            );
        }

        return result.toString();
    }

    public static boolean verifySignature(
            Map<String, String> params,
            String secretKey
    ) {

        Map<String, String> sorted =
                new TreeMap<>();

        for (Map.Entry<String, String> entry
                : params.entrySet()) {

            String key = entry.getKey();

            if ("vnp_SecureHash".equals(key)
                    || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            sorted.put(key, entry.getValue());
        }

        StringBuilder hashData =
                new StringBuilder();

        boolean first = true;

        for (Map.Entry<String, String> entry
                : sorted.entrySet()) {

            if (entry.getValue() == null
                    || entry.getValue().isBlank()) {
                continue;
            }

            if (!first) {
                hashData.append("&");
            }

            first = false;

            hashData.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(entry.getValue()));
        }

        String calculatedHash =
                hmacSha512(
                        secretKey,
                        hashData.toString()
                );

        return calculatedHash.equalsIgnoreCase(
                params.get("vnp_SecureHash")
        );
    }
}