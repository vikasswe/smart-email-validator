package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpfRecordCheck extends AbstractEmailCheck {

    public SpfRecordCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "SPF Record Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(attempt, false, "Invalid email structure.");
        }

        try {
            List<String> txtRecords = DnsLookup.lookup(domain, "TXT");

            List<String> spfRecords = txtRecords.stream()
                    .filter(record ->
                            record.replace("\"", "")
                                    .trim()
                                    .toLowerCase()
                                    .startsWith("v=spf1"))
                    .toList();

            if (spfRecords.size() == 1) {
                return result(
                        attempt,
                        true,
                        "SPF record found: " + spfRecords.get(0)
                );
            }

            if (spfRecords.isEmpty()) {
                return result(
                        attempt,
                        false,
                        "No SPF record found."
                );
            }

            return result(
                    attempt,
                    false,
                    "Multiple SPF records found. This is an SPF configuration error."
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "SPF lookup failed: " + exception.getMessage()
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