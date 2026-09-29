package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface EmailCheckResultRepository
        extends JpaRepository<EmailCheckResult, UUID> {
}