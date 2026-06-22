package com.app.auth.config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class QueryParamIdCrypto {
    private static final String PREFIX = "qid_";
    private static final String KEY = "KEDULZ_QUERY_ID_V1";

    private QueryParamIdCrypto() {
    }

    public static Long decodeLong(String value) {
        return Long.parseLong(decodeString(value, "id"));
    }

    public static Integer decodeInteger(String value) {
        return Integer.parseInt(decodeString(value, "id"));
    }

    public static String decodeString(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing " + parameterName + " parameter");
        }

        String raw = value.trim();
        if (!raw.startsWith(PREFIX)) {
            return raw;
        }

        String encoded = raw.substring(PREFIX.length());
        byte[] encrypted = Base64.getUrlDecoder().decode(addPadding(encoded));
        byte[] key = KEY.getBytes(StandardCharsets.UTF_8);
        byte[] decrypted = new byte[encrypted.length];

        for (int i = 0; i < encrypted.length; i++) {
            decrypted[i] = (byte) (encrypted[i] ^ key[i % key.length]);
        }

        String decoded = new String(decrypted, StandardCharsets.UTF_8);
        if (!decoded.startsWith("v1:")) {
            throw new IllegalArgumentException("Invalid encrypted " + parameterName + " parameter");
        }

        return decoded.substring(3);
    }

    private static String addPadding(String value) {
        int remainder = value.length() % 4;
        return remainder == 0 ? value : value + "=".repeat(4 - remainder);
    }
}
