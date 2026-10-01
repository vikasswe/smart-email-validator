package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DmarcRecordCheck extends AbstractEmailCheck {

    public DmarcRecordCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "DMARC Record Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(attempt, false, "Invalid email structure.");
        }

        try {
            String dmarcDomain = "_dmarc." + domain;
            List<String> txtRecords =
                    DnsLookup.lookup(dmarcDomain, "TXT");

            List<String> dmarcRecords = txtRecords.stream()
                    .filter(record ->
                            record.replace("\"", "")
                                    .trim()
                                    .toLowerCase()
                                    .startsWith("v=dmarc1"))
                    .toList();

            if (dmarcRecords.size() == 1) {
                return result(
                        attempt,
                        true,
                        "DMARC record found: " + dmarcRecords.get(0)
                );
            }

            if (dmarcRecords.isEmpty()) {
                return result(
                        attempt,
                        false,
                        "No DMARC record found."
                );
            }

            return result(
                    attempt,
                    false,
                    "Multiple DMARC records found."
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "DMARC lookup failed: " + exception.getMessage()
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