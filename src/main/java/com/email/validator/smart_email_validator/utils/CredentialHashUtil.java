package com.email.validator.smart_email_validator.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class CredentialHashUtil {

    private CredentialHashUtil() {}

    public static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 is not available", e
            );
        }
    }

    public static boolean matches(String rawSecret, String storedHash) {
        if (rawSecret == null || storedHash == null) {
            return false;
        }

        byte[] actual = sha256(rawSecret)
                .getBytes(StandardCharsets.UTF_8);
        byte[] expected = storedHash
                .getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(actual, expected);
    }
}