package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.DnsLookup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DomainDnsCheck extends AbstractEmailCheck {

    public DomainDnsCheck(CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Domain DNS Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String domain = extractDomain(attempt.getEmail());

        if (domain == null) {
            return result(
                    attempt,
                    false,
                    "Cannot perform DNS lookup: email structure is invalid."
            );
        }

        try {
            List<String> aRecords = DnsLookup.lookup(domain, "A");
            List<String> aaaaRecords = DnsLookup.lookup(domain, "AAAA");

            if (!aRecords.isEmpty() || !aaaaRecords.isEmpty()) {
                return result(
                        attempt,
                        true,
                        "Domain resolves through DNS. A records: "
                                + aRecords.size()
                                + ", AAAA records: "
                                + aaaaRecords.size()
                );
            }

            return result(
                    attempt,
                    false,
                    "Domain has no A or AAAA records."
            );
        } catch (Exception exception) {
            return result(
                    attempt,
                    false,
                    "DNS lookup failed: " + exception.getMessage()
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