package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class RoleBasedEmailCheck extends AbstractEmailCheck {

    private static final Set<String> ROLE_NAMES = Set.of(
            "admin",
            "administrator",
            "support",
            "sales",
            "info",
            "contact",
            "help",
            "helpdesk",
            "service",
            "billing",
            "accounts",
            "finance",
            "marketing",
            "security",
            " webmaster",
            "postmaster",
            "abuse",
            "noreply",
            "no-reply",
            "office",
            "team",
            "hr",
            "jobs",
            "careers"
    );

    public RoleBasedEmailCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Role Based Email Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String email = attempt.getEmail();

        if (email == null || email.isBlank()) {
            return result(attempt, false, "Email address is blank.");
        }

        int atIndex = email.lastIndexOf('@');

        if (atIndex <= 0 || atIndex != email.indexOf('@')) {
            return result(attempt, false, "Invalid email structure.");
        }

        String localPart = email.substring(0, atIndex)
                .toLowerCase()
                .trim();

        if (ROLE_NAMES.contains(localPart)) {
            return result(
                    attempt,
                    false,
                    "Role-based email address detected: " + localPart
            );
        }

        return result(
                attempt,
                true,
                "Email address does not match the configured role names."
        );
    }
}