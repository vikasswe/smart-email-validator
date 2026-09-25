package com.email.validator.smart_email_validator.utils;

public final class EmailUtils {

    private EmailUtils() {
    }

    public static String normalize(String email) {

        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    public static String extractDomain(String email) {

        if (email == null) {
            return null;
        }

        int atIndex = email.lastIndexOf('@');

        if (atIndex <= 0 || atIndex == email.length() - 1) {
            return null;
        }

        return email.substring(atIndex + 1).toLowerCase();
    }
}