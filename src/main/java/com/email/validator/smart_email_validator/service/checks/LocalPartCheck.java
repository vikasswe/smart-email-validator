package com.email.validator.smart_email_validator.service.checks;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class LocalPartCheck extends AbstractEmailCheck {

    private static final Pattern LOCAL_PART_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+"
                            + "(\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*$"
            );

    public LocalPartCheck(
            CheckTypeRepository checkTypeRepository) {
        super(checkTypeRepository);
    }

    @Override
    public String checkName() {
        return "Local Part Validation";
    }

    @Override
    public EmailCheckResult run(CheckedEmail attempt) {
        String email = attempt.getEmail();

        if (email == null) {
            return result(attempt, false, "Email is null.");
        }

        int atIndex = email.indexOf('@');

        if (atIndex <= 0 || atIndex != email.lastIndexOf('@')) {
            return result(
                    attempt,
                    false,
                    "Cannot validate local part: email structure is invalid."
            );
        }

        String localPart = email.substring(0, atIndex);

        if (localPart.length() > 64) {
            return result(
                    attempt,
                    false,
                    "Local part exceeds 64 characters."
            );
        }

        if (!LOCAL_PART_PATTERN.matcher(localPart).matches()) {
            return result(
                    attempt,
                    false,
                    "Local part contains unsupported characters or dot placement."
            );
        }

        return result(
                attempt,
                true,
                "Local part is valid."
        );
    }
}