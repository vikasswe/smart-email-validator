package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.response.ClientCredentialResponse;
import com.email.validator.smart_email_validator.entity.ClientCredential;
import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.repository.ClientCredentialRepository;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientCredentialService {

    private final ClientCredentialRepository credentialRepository;
    private final UserSubscriptionRepository subscriptionRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ClientCredentialResponse generate(
            UUID userId,
            UUID subscriptionId
    ) {
        UserSubscription subscription = getValidSubscription(
                userId,
                subscriptionId
        );

        Integer allowedGenerations =
                subscription.getNumOfClientIdSecretGenerateSnapshot();

        if (allowedGenerations == null || allowedGenerations <= 0) {
            throw new IllegalArgumentException(
                    "This subscription does not allow credential generation."
            );
        }

        long usedGenerations =
                credentialRepository.countBySubscriptionId(subscriptionId);

        if (usedGenerations >= allowedGenerations) {
            throw new IllegalArgumentException(
                    "Credential generation limit reached for this subscription."
            );
        }

        // Deactivate the previous active credential for this subscription.
        List<ClientCredential> existingCredentials =
                credentialRepository.findBySubscriptionId(subscriptionId);

        existingCredentials.stream()
                .filter(credential -> Boolean.TRUE.equals(credential.getActive()))
                .forEach(credential -> {
                    credential.setActive(false);
                    credential.setRemarks(
                            "Deactivated because a new credential was generated."
                    );
                });

        String clientId = generateUniqueClientId();
        String clientSecret = generateSecret();

        ClientCredential credential = ClientCredential.builder()
                .subscription(subscription)
                .clientId(clientId)
                .clientSecretHash(hashSecret(clientSecret))
                .active(true)
                .remarks("Credential generated.")
                .build();

        ClientCredential saved = credentialRepository.save(credential);

        return toResponse(saved, userId, clientSecret);
    }

    @Transactional(readOnly = true)
    public List<ClientCredentialResponse> getBySubscription(
            UUID userId,
            UUID subscriptionId
    ) {
        getSubscriptionForUser(userId, subscriptionId);

        return credentialRepository.findBySubscriptionId(subscriptionId)
                .stream()
                .map(credential -> toResponse(
                        credential,
                        userId,
                        null
                ))
                .toList();
    }

    private UserSubscription getValidSubscription(
            UUID userId,
            UUID subscriptionId
    ) {
        UserSubscription subscription =
                getSubscriptionForUser(userId, subscriptionId);

        Instant now = Instant.now();

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Subscription is not active."
            );
        }

        if (subscription.getEndAt() != null
                && !subscription.getEndAt().isAfter(now)) {
            throw new IllegalArgumentException(
                    "Subscription has expired."
            );
        }

        return subscription;
    }

    private UserSubscription getSubscriptionForUser(
            UUID userId,
            UUID subscriptionId
    ) {
        UserSubscription subscription =
                subscriptionRepository.findById(subscriptionId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Subscription not found: " + subscriptionId
                        ));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Subscription does not belong to this user."
            );
        }

        return subscription;
    }

    private String generateUniqueClientId() {
        String clientId;

        do {
            clientId = "cl_" + randomToken(24);
        } while (credentialRepository.existsByClientId(clientId));

        return clientId;
    }

    private String generateSecret() {
        return "cs_" + randomToken(48);
    }

    private String randomToken(int byteCount) {
        byte[] bytes = new byte[byteCount];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashSecret(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    secret.getBytes(StandardCharsets.UTF_8)
            );

            return java.util.HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to hash client secret.",
                    exception
            );
        }
    }

    private ClientCredentialResponse toResponse(
            ClientCredential credential,
            UUID userId,
            String plainSecret
    ) {
        return ClientCredentialResponse.builder()
                .id(credential.getId())
                .userId(userId)
                .subscriptionId(credential.getSubscription().getId())
                .clientId(credential.getClientId())
                .clientSecret(plainSecret)
                .active(credential.getActive())
                .remarks(credential.getRemarks())
                .createdAt(credential.getCreatedAt())
                .build();
    }


    @Transactional(readOnly = true)
    public List<ClientCredentialResponse> getAll() {
        return credentialRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientCredentialResponse> getByUserId(UUID userId) {
        return credentialRepository
                .findBySubscriptionUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClientCredentialResponse> getBySubscriptionId(
            UUID subscriptionId
    ) {
        return credentialRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ClientCredentialResponse activate(UUID id) {
        ClientCredential credential = getCredential(id);

        if (Boolean.TRUE.equals(credential.getActive())) {
            return toResponse(credential);
        }

        // Only one active credential per subscription.
        boolean anotherActiveExists = credentialRepository
                .findBySubscriptionIdOrderByCreatedAtDesc(
                        credential.getSubscription().getId()
                )
                .stream()
                .anyMatch(other ->
                        !other.getId().equals(id)
                                && Boolean.TRUE.equals(other.getActive())
                );

        if (anotherActiveExists) {
            throw new IllegalArgumentException(
                    "Another credential is already active for this subscription."
            );
        }

        credential.setActive(true);
        credential.setRemarks("Credential activated by admin.");

        return toResponse(credentialRepository.save(credential));
    }

    public ClientCredentialResponse deactivate(UUID id) {
        ClientCredential credential = getCredential(id);

        credential.setActive(false);
        credential.setRemarks("Credential deactivated by admin.");

        return toResponse(credentialRepository.save(credential));
    }

    public void delete(UUID id) {
        ClientCredential credential = getCredential(id);
        credentialRepository.delete(credential);
    }

    private ClientCredential getCredential(UUID id) {
        return credentialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Client credential not found: " + id
                ));
    }

    private ClientCredentialResponse toResponse(
            ClientCredential credential
    ) {
        return ClientCredentialResponse.builder()
                .id(credential.getId())
                .userId(credential.getSubscription().getUser().getId())
                .subscriptionId(credential.getSubscription().getId())
                .clientId(credential.getClientId())
                // Never expose the secret or its hash.
                .clientSecret(null)
                .active(credential.getActive())
                .remarks(credential.getRemarks())
                .createdAt(credential.getCreatedAt())
                .build();
    }
}