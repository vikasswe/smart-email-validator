package com.email.validator.smart_email_validator.exception;

public class SubscriptionQuotaExceededException extends RuntimeException {

    public SubscriptionQuotaExceededException(String message) {
        super(message);
    }
}
