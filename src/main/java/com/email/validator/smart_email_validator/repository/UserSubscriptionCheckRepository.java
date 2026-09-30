package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.UserSubscriptionCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSubscriptionCheckRepository
        extends JpaRepository<UserSubscriptionCheck, UUID> {

    List<UserSubscriptionCheck> findBySubscriptionId(UUID subscriptionId);

}