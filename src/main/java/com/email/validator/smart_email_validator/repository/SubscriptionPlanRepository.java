package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
}