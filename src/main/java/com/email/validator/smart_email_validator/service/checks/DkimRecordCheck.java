package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DkimRecordCheck extends AbstractEmailCheck {

    private static final List<String> SELECTORS = List.of(
            "default",
            "selector1",
            "selector2",
            "google",
            "s1",
            "s2",
            "k1",
            "mail"
    );

    public DkimRecordCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "DKIM Record Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(attempt, false, "Invalid email structure.");
        }

        try {
            for (String selector : SELECTORS) {
                String recordName =
                        selector + "._domainkey." + domain;

                List<String> records =
                        DnsLookup.lookup(recordName, "TXT");

                if (records.stream().anyMatch(record ->
                        record.replace("\"", "")
                                .toLowerCase()
                                .contains("v=dkim1")
                                || record.replace("\"", "")
                                .contains("p="))) {
                    return result(
                            attempt,
                            true,
                            "A possible DKIM record was found for selector: "
                                    + selector
                    );
                }
            }

            return result(
                    attempt,
                    false,
                    "No DKIM record found for the configured selectors. "
                            + "The domain may use another selector."
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "DKIM lookup failed: " + exception.getMessage()
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