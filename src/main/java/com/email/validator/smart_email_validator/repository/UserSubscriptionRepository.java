package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    Optional<UserSubscription>
    findFirstByUserIdAndStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            UUID userId,
            SubscriptionStatus status,
            Instant currentTime1,
            Instant currentTime2
    );
}