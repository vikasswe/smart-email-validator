package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.request.UserSubscriptionCreateRequest;
import com.email.validator.smart_email_validator.dto.response.UserSubscriptionResponse;
import com.email.validator.smart_email_validator.entity.*;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.repository.SubscriptionPlanCheckRepository;
import com.email.validator.smart_email_validator.repository.SubscriptionPlanRepository;
import com.email.validator.smart_email_validator.repository.UserRepository;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSubscriptionService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanCheckRepository planCheckRepository;

    public UserSubscriptionResponse subscribe(
            UserSubscriptionCreateRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + request.userId()
                        ));

        SubscriptionPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Subscription plan not found: "
                                        + request.planId()
                        ));

        if (!Boolean.TRUE.equals(plan.getActive())) {
            throw new IllegalArgumentException(
                    "Subscription plan is inactive"
            );
        }

        Instant now = Instant.now();

        Optional<UserSubscription> existing =
                subscriptionRepository
                        .findFirstByUserIdAndPlanIdAndStatusOrderByStartAtDesc(
                                user.getId(),
                                plan.getId(),
                                SubscriptionStatus.ACTIVE
                        );

        if (existing.isPresent()) {
            UserSubscription current = existing.get();

            if (current.getEndAt() == null
                    || current.getEndAt().isAfter(now)) {

                current.setStatus(SubscriptionStatus.CANCELLED);
                current.setRemarks(
                        "Cancelled because the user resubscribed to the same plan."
                );
            } else {
                current.setStatus(SubscriptionStatus.EXPIRED);
                current.setRemarks(
                        "Subscription expired before the user resubscribed."
                );
            }

            subscriptionRepository.save(current);
        }

        Instant endAt = plan.getPlanDurationDays() == null
                ? null
                : now.plus(
                plan.getPlanDurationDays(),
                ChronoUnit.DAYS
        );

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .priceSnapshot(plan.getPrice())
                .billingCycleSnapshot(plan.getBillingCycle())
                .maxEmailsPerRequestSnapshot(
                        plan.getMaxEmailsPerRequest()
                )
                .emailCheckPerMinuteSnapshot(
                        plan.getEmailCheckPerMinute()
                )
                .totalEmailCheckTillExpirySnapshot(
                        plan.getTotalEmailCheckTillExpiry()
                )
                .numOfClientIdSecretGenerateSnapshot(
                        plan.getNumOfClientIdSecretGenerate()
                )
                .startAt(now)
                .endAt(endAt)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        List<SubscriptionPlanCheck> planChecks =
                planCheckRepository.findByPlanId(plan.getId());

        List<UserSubscriptionCheck> userChecks = planChecks.stream()
                .map(planCheck -> UserSubscriptionCheck.builder()
                        .subscription(subscription)
                        .checkType(planCheck.getCheckType())
                        .enabled(planCheck.getDefaultEnabled())
                        .build())
                .toList();

        subscription.setChecks(new ArrayList<>(userChecks));

        UserSubscription saved =
                subscriptionRepository.save(subscription);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserSubscriptionResponse getById(UUID id) {

        UserSubscription subscription =
                subscriptionRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscription not found: " + id
                                ));

        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public UserSubscriptionResponse getActiveByUserId(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId
                        ));

        Instant now = Instant.now();

        UserSubscription subscription =
                subscriptionRepository
                        .findFirstByUserIdAndStatusOrderByStartAtDesc(
                                user.getId(),
                                SubscriptionStatus.ACTIVE
                        )
                        .filter(s ->
                                s.getEndAt() == null
                                        || s.getEndAt().isAfter(now)
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active subscription found"
                                ));

        return toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getByUserId(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found: " + userId
            );
        }

        return subscriptionRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getAll() {

        return subscriptionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserSubscriptionResponse toResponse(
            UserSubscription subscription) {

        List<UserSubscriptionResponse.UserSubscriptionCheckResponse> checks =
                subscription.getChecks()
                        .stream()
                        .map(check -> {
                            CheckType checkType = check.getCheckType();

                            return UserSubscriptionResponse.UserSubscriptionCheckResponse
                                    .builder()
                                    .id(check.getId())
                                    .checkTypeId(checkType.getId())
                                    .checkName(checkType.getName())
                                    .weight(checkType.getDefaultWeight())
                                    .estimatedTimeMs(checkType.getEstimatedTimeMs())
                                    .enabled(check.getEnabled())
                                    .build();
                        })
                        .toList();

        SubscriptionPlan plan = subscription.getPlan();

        return UserSubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser().getId())

                .planId(plan.getId())
                .planName(plan.getName())
                .priority(plan.getPriority())
                .price(subscription.getPriceSnapshot())
                .billingCycle(subscription.getBillingCycleSnapshot())

                .maxEmailsPerRequest(
                        subscription.getMaxEmailsPerRequestSnapshot()
                )
                .emailCheckPerMinute(
                        subscription.getEmailCheckPerMinuteSnapshot()
                )
                .totalEmailCheckTillExpiry(
                        subscription.getTotalEmailCheckTillExpirySnapshot()
                )
                .numOfClientIdSecretGenerate(
                        subscription.getNumOfClientIdSecretGenerateSnapshot()
                )
                .totalEmailCheckTillNow(
                        subscription.getTotalEmailCheckTillNow()
                )
                .startAt(subscription.getStartAt())
                .endAt(subscription.getEndAt())
                .status(subscription.getStatus())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .checks(checks)
                .build();
    }
}