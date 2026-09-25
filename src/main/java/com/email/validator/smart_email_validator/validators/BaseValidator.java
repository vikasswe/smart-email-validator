package com.email.validator.smart_email_validator.validators;

import com.email.validator.smart_email_validator.entity.ValidationDetail;

import java.time.Instant;

public abstract  class BaseValidator {

    protected ValidationDetail build(String name, boolean passed, String message, double score) {
        ValidationDetail vd = new ValidationDetail();
        vd.setCheckName(name);
//        vd.setIsPassed(passed);
        vd.setMessage(message);
//        vd.setImpactScore(score);
//        vd.setCheckedAt(Instant.now());

        return vd;
    }

}
