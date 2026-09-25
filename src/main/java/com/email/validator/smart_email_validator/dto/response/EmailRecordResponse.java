package com.email.validator.smart_email_validator.dto.response;

import java.util.UUID;

public record EmailRecordResponse(
        UUID id,
        String email,
        String domain
) {}