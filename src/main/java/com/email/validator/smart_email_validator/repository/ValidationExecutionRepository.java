package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.ValidationExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ValidationExecutionRepository extends JpaRepository<ValidationExecution, UUID> {

    Optional<ValidationExecution>
    findTopByEmailRecordIdOrderByStartedAtDesc(UUID emailRecordId);
}