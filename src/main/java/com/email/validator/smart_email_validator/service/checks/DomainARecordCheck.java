package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DomainARecordCheck extends AbstractEmailCheck {

    public DomainARecordCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Domain A Record Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(
                    attempt,
                    false,
                    "Cannot check A record: email structure is invalid."
            );
        }

        try {
            List<String> records = DnsLookup.lookup(domain, "A");

            if (records.isEmpty()) {
                return result(
                        attempt,
                        false,
                        "Domain has no IPv4 A record."
                );
            }

            return result(
                    attempt,
                    true,
                    "Domain has IPv4 A record(s): " + String.join(", ", records)
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "A record lookup failed: " + exception.getMessage()
            );
        }
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