package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DomainFormatCheck extends AbstractEmailCheck {

    private static final Pattern LABEL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?$"
            );

    public DomainFormatCheck(
            CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Domain Format Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String email = attempt.getEmail();

        if (email == null) {
            return result(attempt, false, "Email is null.");
        }

        int atIndex = email.lastIndexOf('@');

        if (atIndex <= 0 || atIndex == email.length() - 1
                || email.indexOf('@') != atIndex) {
            return result(
                    attempt,
                    false,
                    "Cannot validate domain: email structure is invalid."
            );
        }

        String domain = email.substring(atIndex + 1);

        if (domain.length() > 253) {
            return result(
                    attempt,
                    false,
                    "Domain exceeds 253 characters."
            );
        }

        if (domain.startsWith(".") || domain.endsWith(".")) {
            return result(
                    attempt,
                    false,
                    "Domain cannot start or end with a dot."
            );
        }

        String[] labels = domain.split("\\.", -1);

        if (labels.length < 2) {
            return result(
                    attempt,
                    false,
                    "Domain must contain a top-level domain."
            );
        }

        for (String label : labels) {
            if (label.isEmpty() || label.length() > 63
                    || !LABEL_PATTERN.matcher(label).matches()) {
                return result(
                        attempt,
                        false,
                        "Domain contains an invalid label: " + label
                );
            }
        }

        return result(
                attempt,
                true,
                "Domain format is valid."
        );
    }
}