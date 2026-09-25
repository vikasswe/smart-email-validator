package com.email.validator.smart_email_validator.dto.response;

import com.email.validator.smart_email_validator.enums.BillingCycle;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
@Builder
public record SubscriptionPlanResponse(

        UUID id,
        String name,
        BigDecimal price,
        BillingCycle billingCycle,
        Long credits,
        Integer maxEmailsPerRequest,
        Integer emailCheckPerMinute,
        Integer totalEmailCheckTillExpiry,
        Integer priority,
        Integer numOfClientIdSecretGenerate,
        Integer planDurationDays,
        Boolean active,
        List<PlanCheckResponse> checks

) {

    @Builder
    public record PlanCheckResponse(

            UUID id,
            UUID checkTypeId,
            String checkName,
            String category,
            Integer defaultWeight,
            Long estimatedTimeMs,
            Boolean checkEnabled,
            Boolean defaultEnabled

    ) {}
}