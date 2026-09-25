package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.EmailRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface EmailRecordRepository
        extends JpaRepository<EmailRecord, UUID> {

    Optional<EmailRecord> findByUserIdAndEmail(
            UUID userId,
            String email
    );

    boolean existsByUserIdAndEmail(
            UUID userId,
            String email
    );
}