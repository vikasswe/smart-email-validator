package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface EmailCheckResultRepository
        extends JpaRepository<EmailCheckResult, UUID> {

    List<EmailCheckResult> findAllByCheckedEmailId(UUID checkedEmailId);
}