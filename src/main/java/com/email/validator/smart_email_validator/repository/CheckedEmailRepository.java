package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.enums.EmailValidationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CheckedEmailRepository
        extends JpaRepository<CheckedEmail, UUID> {
}