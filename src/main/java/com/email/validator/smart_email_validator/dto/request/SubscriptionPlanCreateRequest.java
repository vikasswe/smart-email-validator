package com.email.validator.smart_email_validator.dto.request;

import com.email.validator.smart_email_validator.enums.BillingCycle;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SubscriptionPlanCreateRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal price,

        @NotNull
        BillingCycle billingCycle,

        @NotNull
        @Min(0)
        Long credits,

        @NotNull
        @Min(1)
        Integer maxEmailsPerRequest,

        @NotNull
        @Min(1)
        Integer emailCheckPerMinute,

        @NotNull
        @Min(0)
        Integer totalEmailCheckTillExpiry,

        @NotNull
        @Min(1)
        Integer priority,

        @NotNull
        @Min(0)
        Integer numOfClientIdSecretGenerate,

        @NotNull
        @Min(0)
        Integer planDurationDays,

        @NotNull
        Boolean active,

        @NotNull
        @Valid
        List<PlanCheckRequest> checks

) {

        public record PlanCheckRequest(

                @NotNull
                UUID checkTypeId,

                @NotNull
                Boolean defaultEnabled

        ) {}
}