package com.email.validator.smart_email_validator.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CheckTypeCreateRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        @Min(0)
        Integer defaultWeight,

        @NotNull
        @Min(0)
        Long estimatedTimeMs,

        @NotNull
        Boolean enabled,

        @NotNull
        @Min(0)
        Integer executionOrder
) {}