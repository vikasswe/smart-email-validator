package com.email.validator.smart_email_validator.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EmailValidationResponse(
        UUID requestId,
        int totalEmails,
        int startedEmails,
        int rejectedEmails,
        List<EmailValidationResult> results
) {
    public record EmailValidationResult(
            UUID checkedEmailId,
            String email,
            String status,
            BigDecimal scorePercentage,
            BigDecimal riskPercentage,
            int totalChecks,
            int completedChecks,
            int passedChecks,
            int failedChecks,
            String message,
            List<CheckResult> checks
    ) {
    }

    public record CheckResult(
            UUID checkResultId,
            String checkName,
            BigDecimal weight,
            String status,
            Boolean passed,
            BigDecimal scoreEarned,
            String message
    ) {
    }
}