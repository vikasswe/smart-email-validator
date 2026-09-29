package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.enums.CheckResultStatus;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.runner.EmailCheckRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public abstract class AbstractEmailCheck implements EmailCheckRunner {

    private final CheckTypeRepository checkTypeRepository;

    protected AbstractEmailCheck(
            CheckTypeRepository checkTypeRepository
    ) {
        this.checkTypeRepository = checkTypeRepository;
    }

    protected EmailCheckResult result(
            CheckedEmail attempt,
            boolean passed,
            String message
    ) {

        CheckType checkType = checkTypeRepository
                .findByNameIgnoreCase(checkName())
                .orElseThrow(() -> new IllegalStateException(
                        "CheckType not found: " + checkName()
                ));

        BigDecimal weight = BigDecimal.valueOf(
                checkType.getDefaultWeight()
        );

        return EmailCheckResult.builder()
                .checkedEmail(attempt)
                .checkType(checkType)
                .weightSnapshot(weight)
                .status(passed
                        ? CheckResultStatus.PASSED
                        : CheckResultStatus.FAILED)
                .passed(passed)
                .scoreEarned(passed ? weight : BigDecimal.ZERO)
                .resultMessage(message)
                .build();
    }
}