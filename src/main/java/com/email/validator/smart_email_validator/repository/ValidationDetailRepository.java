package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.ValidationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ValidationDetailRepository extends JpaRepository<ValidationDetail, UUID> {

    List<ValidationDetail>
    findByValidationExecutionId(UUID validationExecutionId);
}