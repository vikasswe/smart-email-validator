package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.BlacklistEntry;
import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.enums.BlacklistType;
import com.email.validator.smart_email_validator.repository.BlacklistEntryRepository;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.utils.BlacklistValueUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BlacklistEmailCheck extends AbstractEmailCheck {

    private final BlacklistEntryRepository blacklistEntryRepository;

    public BlacklistEmailCheck(
            CheckTypeRepository checkTypeRepository,
            BlacklistEntryRepository blacklistEntryRepository
    ) {
        super(checkTypeRepository);
        this.blacklistEntryRepository = blacklistEntryRepository;
    }

    @Override
    public String checkName() {
        return "Blacklist Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {

        String email = BlacklistValueUtils.normalize(
                attempt.getEmail()
        );

        if (email == null || email.isBlank()) {
            return result(
                    attempt,
                    false,
                    "Email is empty"
            );
        }

        BlacklistType type =
                BlacklistValueUtils.detectType(email);

        if (type != BlacklistType.EMAIL) {
            return result(
                    attempt,
                    false,
                    "Invalid email format"
            );
        }

        // Check exact email address.
        Optional<BlacklistEntry> emailEntry =
                blacklistEntryRepository.findByValueAndType(
                        email,
                        BlacklistType.EMAIL
                );

        if (emailEntry.isPresent()) {
            return result(
                    attempt,
                    false,
                    "Email address is blacklisted"
            );
        }

        // Extract and normalize the domain.
        String domain = email.substring(
                email.lastIndexOf('@') + 1
        );

        // Check exact domain and all parent domains.
        String currentDomain = domain;

        while (currentDomain != null) {

            Optional<BlacklistEntry> domainEntry =
                    blacklistEntryRepository.findByValueAndType(
                            currentDomain,
                            BlacklistType.DOMAIN
                    );

            if (domainEntry.isPresent()) {
                return result(
                        attempt,
                        false,
                        "Email domain is blacklisted: "
                                + currentDomain
                );
            }

            int dotIndex = currentDomain.indexOf('.');

            if (dotIndex < 0) {
                break;
            }

            currentDomain =
                    currentDomain.substring(dotIndex + 1);
        }

        return result(
                attempt,
                true,
                "Email is not present in the blacklist"
        );
    }
}