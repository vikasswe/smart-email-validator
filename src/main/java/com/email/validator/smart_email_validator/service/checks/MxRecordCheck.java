package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MxRecordCheck extends AbstractEmailCheck {

    public MxRecordCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "MX Record Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(
                    attempt,
                    false,
                    "Cannot check MX records: email structure is invalid."
            );
        }

        try {
            List<String> records = DnsLookup.lookup(domain, "MX");

            if (!records.isEmpty()) {
                return result(
                        attempt,
                        true,
                        "Domain has MX record(s): "
                                + String.join("; ", records)
                );
            }

            List<String> aRecords = DnsLookup.lookup(domain, "A");
            List<String> aaaaRecords = DnsLookup.lookup(domain, "AAAA");

            if (!aRecords.isEmpty() || !aaaaRecords.isEmpty()) {
                return result(
                        attempt,
                        false,
                        "No MX records found. Domain has address records, "
                                + "but mail delivery via address fallback "
                                + "has not been verified."
                );
            }

            return result(
                    attempt,
                    false,
                    "Domain has no MX records or address records."
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "MX lookup failed: " + exception.getMessage()
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