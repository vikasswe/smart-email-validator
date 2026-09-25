package com.email.validator.smart_email_validator.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record CheckTypeResponse(
        UUID id,
        String name,
        Integer defaultWeight,
        Long estimatedTimeMs,
        Boolean enabled,
        Integer executionOrder
) {}