package com.email.validator.smart_email_validator.utils;

import com.email.validator.smart_email_validator.enums.BlacklistType;

public final class BlacklistValueUtils {

    private BlacklistValueUtils() {
    }

    public static String normalize(String value) {

        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase();
    }

    public static BlacklistType detectType(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = normalize(value);

        int firstAt = normalized.indexOf('@');
        int lastAt = normalized.lastIndexOf('@');

        if (firstAt > 0 && firstAt == lastAt
                && firstAt < normalized.length() - 1) {

            return BlacklistType.EMAIL;
        }

        return BlacklistType.DOMAIN;
    }
}