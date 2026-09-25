package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.SubscriptionPlanCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPlanCheckRepository extends JpaRepository<SubscriptionPlanCheck, UUID> {

    List<SubscriptionPlanCheck> findByPlanId(UUID planId);

}