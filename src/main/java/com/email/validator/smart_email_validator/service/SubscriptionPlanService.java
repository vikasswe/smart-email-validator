package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.request.SubscriptionPlanCreateRequest;
import com.email.validator.smart_email_validator.dto.response.SubscriptionPlanResponse;
import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.entity.SubscriptionPlan;
import com.email.validator.smart_email_validator.entity.SubscriptionPlanCheck;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.repository.SubscriptionPlanCheckRepository;
import com.email.validator.smart_email_validator.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository planRepository;
    private final SubscriptionPlanCheckRepository planCheckRepository;
    private final CheckTypeRepository checkTypeRepository;

    public SubscriptionPlanResponse create(
            SubscriptionPlanCreateRequest request) {

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .name(request.name().trim())
                .price(request.price())
                .billingCycle(request.billingCycle())
                .credits(request.credits())
                .maxEmailsPerRequest(
                        request.maxEmailsPerRequest()
                )
                .emailCheckPerMinute(
                        request.emailCheckPerMinute()
                )
                .totalEmailCheckTillExpiry(
                        request.totalEmailCheckTillExpiry()
                )
                .priority(request.priority())
                .numOfClientIdSecretGenerate(
                        request.numOfClientIdSecretGenerate()
                )
                .planDurationDays(request.planDurationDays())
                .active(request.active())
                .build();

        plan = planRepository.save(plan);

        List<SubscriptionPlanCheck> planChecks =
                new ArrayList<>();

        for (SubscriptionPlanCreateRequest.PlanCheckRequest checkRequest
                : request.checks()) {

            CheckType checkType =
                    checkTypeRepository.findById(
                            checkRequest.checkTypeId()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "Check type not found: "
                                            + checkRequest.checkTypeId()
                            ));

            if (!checkType.getEnabled()) {
                throw new IllegalArgumentException(
                        "Check type is disabled: "
                                + checkType.getName()
                );
            }

            SubscriptionPlanCheck planCheck =
                    SubscriptionPlanCheck.builder()
                            .plan(plan)
                            .checkType(checkType)
                            .defaultEnabled(
                                    checkRequest.defaultEnabled()
                            )
                            .build();

            planChecks.add(planCheck);
        }

        planCheckRepository.saveAll(planChecks);

        return getById(plan.getId());
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getById(UUID id) {

        SubscriptionPlan plan =
                planRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Subscription plan not found: "
                                                + id
                                ));

        return toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAll() {

        return planRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SubscriptionPlanResponse toResponse(
            SubscriptionPlan plan) {

        List<SubscriptionPlanCheck> checks =
                planCheckRepository.findByPlanId(
                        plan.getId()
                );

        List<SubscriptionPlanResponse.PlanCheckResponse>
                checkResponses = checks.stream()
                .map(check -> {

                    CheckType checkType =
                            check.getCheckType();

                    return SubscriptionPlanResponse
                            .PlanCheckResponse
                            .builder()
                            .id(check.getId())
                            .checkTypeId(
                                    checkType.getId()
                            )
                            .checkName(
                                    checkType.getName()
                            )
                            .defaultWeight(
                                    checkType.getDefaultWeight()
                            )
                            .estimatedTimeMs(
                                    checkType.getEstimatedTimeMs()
                            )
                            .checkEnabled(
                                    checkType.getEnabled()
                            )
                            .defaultEnabled(
                                    check.getDefaultEnabled()
                            )
                            .build();
                })
                .toList();

        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .billingCycle(plan.getBillingCycle())
                .credits(plan.getCredits())
                .maxEmailsPerRequest(
                        plan.getMaxEmailsPerRequest()
                )
                .emailCheckPerMinute(
                        plan.getEmailCheckPerMinute()
                )
                .totalEmailCheckTillExpiry(
                        plan.getTotalEmailCheckTillExpiry()
                )
                .priority(plan.getPriority())
                .numOfClientIdSecretGenerate(
                        plan.getNumOfClientIdSecretGenerate()
                )
                .planDurationDays(plan.getPlanDurationDays())
                .active(plan.getActive())
                .checks(checkResponses)
                .build();
    }
}