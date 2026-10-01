package com.email.validator.smart_email_validator.dto.response;

import com.email.validator.smart_email_validator.enums.BillingCycle;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record UserSubscriptionResponse(
        UUID id,
        UUID userId,

        UUID planId,
        String planName,
        Integer priority,
        java.math.BigDecimal price,
        BillingCycle billingCycle,

        Integer maxEmailsPerRequest,
        Integer emailCheckPerMinute,
        Integer totalEmailCheckTillExpiry,
        Integer numOfClientIdSecretGenerate,
        Integer totalEmailCheckTillNow,

        Instant startAt,
        Instant endAt,
        SubscriptionStatus status,

        Instant createdAt,
        Instant updatedAt,

        List<UserSubscriptionCheckResponse> checks
) {
    @Builder
    public record UserSubscriptionCheckResponse(
            UUID id,
            UUID checkTypeId,
            String checkName,
            Integer weight,
            Long estimatedTimeMs,
            Boolean enabled
    ) {
    }
}