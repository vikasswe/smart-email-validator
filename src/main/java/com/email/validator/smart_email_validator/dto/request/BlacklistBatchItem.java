package com.email.validator.smart_email_validator.dto.request;

public record BlacklistBatchItem(
        String value,
        String type,
        String reason
) {
}