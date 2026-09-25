package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.BlacklistEntry;
import com.email.validator.smart_email_validator.enums.BlacklistType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BlacklistEntryRepository
        extends JpaRepository<BlacklistEntry, UUID> {

    Optional<BlacklistEntry> findByValueAndType(
            String value,
            BlacklistType type
    );

    boolean existsByValueAndType(
            String value,
            BlacklistType type
    );

    Page<BlacklistEntry> findByValueContainingIgnoreCase(
            String value,
            Pageable pageable
    );

    Page<BlacklistEntry> findByType(
            BlacklistType type,
            Pageable pageable
    );

    Page<BlacklistEntry> findByTypeAndValueContainingIgnoreCase(
            BlacklistType type,
            String value,
            Pageable pageable
    );
}