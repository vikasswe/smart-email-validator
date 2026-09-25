package com.email.validator.smart_email_validator.utils;

import com.email.validator.smart_email_validator.enums.BlacklistType;

import java.util.Locale;
import java.util.regex.Pattern;

public final class BlacklistValueUtils {

    private BlacklistValueUtils() {
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[^\\s@]+@[^\\s@]+$"
            );

    private static final Pattern DOMAIN_PATTERN =
            Pattern.compile(
                    "^(?=.{1,253}$)(?:[a-zA-Z0-9]"
                            + "(?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+"
                            + "[a-zA-Z]{2,63}$"
            );

    public static String normalize(String value) {

        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    public static BlacklistType detectType(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized =
                normalize(value);

        if (EMAIL_PATTERN.matcher(normalized).matches()) {
            return BlacklistType.EMAIL;
        }

        if (DOMAIN_PATTERN.matcher(normalized).matches()) {
            return BlacklistType.DOMAIN;
        }

        return null;
    }
}