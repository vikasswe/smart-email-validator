package com.email.validator.smart_email_validator.dto.response;


import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ClientCredentialResponse(
        UUID id,
        UUID userId,
        UUID subscriptionId,
        String clientId,
        String clientSecret,
        Boolean active,
        String remarks,
        Instant createdAt
) {}