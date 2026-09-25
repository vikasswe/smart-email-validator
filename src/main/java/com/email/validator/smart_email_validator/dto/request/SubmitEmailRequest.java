package com.email.validator.smart_email_validator.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SubmitEmailRequest(
        UUID userId,

        @NotEmpty
        @Size(max = 1000)
        List<String> emails
) {}