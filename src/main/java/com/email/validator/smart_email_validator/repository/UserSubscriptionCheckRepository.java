package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.UserSubscriptionCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSubscriptionCheckRepository extends JpaRepository<UserSubscriptionCheck, UUID> {

    List<UserSubscriptionCheck>
    findBySubscriptionIdAndAllowedTrueAndEnabledTrueOrderByCheckType_ExecutionOrderAsc(
            UUID subscriptionId
    );

    List<UserSubscriptionCheck>
    findBySubscriptionIdOrderByCheckType_ExecutionOrderAsc(
            UUID subscriptionId
    );
}