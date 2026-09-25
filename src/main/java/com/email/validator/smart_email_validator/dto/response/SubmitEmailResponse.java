package com.email.validator.smart_email_validator.dto.response;

import java.util.List;

public record SubmitEmailResponse(
        Integer totalRequested,
        Integer saved,
        Integer alreadyExists,
        List<EmailRecordResponse> emails
) {}