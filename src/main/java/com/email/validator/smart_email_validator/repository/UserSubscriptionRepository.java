package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository
        extends JpaRepository<UserSubscription, UUID> {

    boolean existsByUserIdAndStatus(
            UUID userId,
            SubscriptionStatus status
    );

    Optional<UserSubscription> findFirstByUserIdAndStatusOrderByStartAtDesc(
            UUID userId,
            SubscriptionStatus status
    );

    List<UserSubscription> findByUserId(UUID userId);
}