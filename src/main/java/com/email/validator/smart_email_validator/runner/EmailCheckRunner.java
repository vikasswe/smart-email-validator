package com.email.validator.smart_email_validator.runner;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;

public interface EmailCheckRunner {

    String checkName();

    EmailCheckResult run(CheckedEmail attempt) throws Exception;

}