package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.ClientCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientCredentialRepository
        extends JpaRepository<ClientCredential, UUID> {

    boolean existsByClientId(String clientId);

    long countBySubscriptionId(UUID subscriptionId);

    List<ClientCredential> findBySubscriptionId(UUID subscriptionId);

    Optional<ClientCredential> findByClientIdAndActiveTrue(String clientId);

    Optional<ClientCredential> findByIdAndSubscriptionId(
            UUID id,
            UUID subscriptionId
    );

    List<ClientCredential> findBySubscriptionUserIdOrderByCreatedAtDesc(
            UUID userId
    );

    List<ClientCredential> findBySubscriptionIdOrderByCreatedAtDesc(
            UUID subscriptionId
    );

    List<ClientCredential> findAllByOrderByCreatedAtDesc();

    Optional<ClientCredential> findByClientId(String clientId);
}