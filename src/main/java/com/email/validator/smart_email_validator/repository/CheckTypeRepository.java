package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.CheckType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckTypeRepository extends JpaRepository<CheckType, UUID> {

    Optional<CheckType> findByNameIgnoreCase(String name);

    Page<CheckType> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    List<CheckType> findByEnabledTrue();
}