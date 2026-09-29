package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

@Component
public class EmailSyntaxCheck extends AbstractEmailCheck {

    public EmailSyntaxCheck(
            CheckTypeRepository checkTypeRepository
    ) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Email Syntax Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String email = attempt.getEmail();

        if (email == null || email.isBlank()) {
            return result(attempt, false, "Email is empty.");
        }

        int atIndex = email.indexOf('@');

        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')
                || atIndex == email.length() - 1) {
            return result(
                    attempt,
                    false,
                    "Email must contain exactly one @ with text on both sides."
            );
        }

        return result(
                attempt,
                true,
                "Email has the basic required structure."
        );
    }
}