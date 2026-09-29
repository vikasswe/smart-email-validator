package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

@Component
public class EmailLengthCheck extends AbstractEmailCheck {

    public EmailLengthCheck(
            CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Email Length Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String email = attempt.getEmail();

        if (email == null || email.isEmpty()) {
            return result(attempt, false, "Email is empty.");
        }

        if (email.length() > 254) {
            return result(
                    attempt,
                    false,
                    "Email exceeds the maximum length of 254 characters."
            );
        }

        return result(
                attempt,
                true,
                "Email length is within the supported limit."
        );
    }
}