package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FreeEmailProviderCheck extends AbstractEmailCheck {

    private static final Set<String> FREE_EMAIL_DOMAINS = Set.of(
            "gmail.com",
            "googlemail.com",
            "yahoo.com",
            "yahoo.co.in",
            "outlook.com",
            "hotmail.com",
            "live.com",
            "msn.com",
            "icloud.com",
            "me.com",
            "aol.com",
            "proton.me",
            "protonmail.com",
            "zoho.com",
            "rediffmail.com"
    );

    public FreeEmailProviderCheck(
            CheckTypeRepository checkTypeRepository
    ) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Free Email Provider Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(attempt, false, "Invalid email structure.");
        }

        if (FREE_EMAIL_DOMAINS.contains(domain)) {
            return result(
                    attempt,
                    true,
                    "Email uses a recognized free email provider: " + domain
            );
        }

        return result(
                attempt,
                false,
                "Email domain is not in the configured free-provider list."
        );
    }

    private String extractDomain(String email) {
        if (email == null) {
            return null;
        }

        int atIndex = email.lastIndexOf('@');

        if (atIndex <= 0
                || atIndex != email.indexOf('@')
                || atIndex == email.length() - 1) {
            return null;
        }

        return email.substring(atIndex + 1).toLowerCase();
    }
}