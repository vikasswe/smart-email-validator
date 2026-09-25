package com.email.validator.smart_email_validator.dto.response;

import com.email.validator.smart_email_validator.enums.UserStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record UserResponse(

        UUID id,
        String email,
        String username,
        UserStatus status,
        Instant createdAt

) {
}