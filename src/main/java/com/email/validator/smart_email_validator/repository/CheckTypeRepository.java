package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.CheckType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CheckTypeRepository extends JpaRepository<CheckType, UUID> {

    Page<CheckType> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}