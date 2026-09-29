package com.email.validator.smart_email_validator.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EmailValidationRequest(
        @NotEmpty
        @Size(max = 1000)
        List<@NotBlank @Size(max = 320) String> emails
) {
}