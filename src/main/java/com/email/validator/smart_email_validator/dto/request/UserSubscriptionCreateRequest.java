package com.email.validator.smart_email_validator.dto.request;


import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserSubscriptionCreateRequest(

        @NotNull
        UUID userId,

        @NotNull
        UUID planId

) {}