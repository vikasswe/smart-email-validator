package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.CheckType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckTypeRepository extends JpaRepository<CheckType, UUID> {

    List<CheckType> findByEnabledTrueOrderByExecutionOrderAsc();

    Optional<CheckType> findByCode(String code);

}
