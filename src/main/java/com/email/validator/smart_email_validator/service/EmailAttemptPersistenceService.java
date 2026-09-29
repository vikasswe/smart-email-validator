package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.CheckResultStatus;
import com.email.validator.smart_email_validator.enums.EmailValidationStatus;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.exception.SubscriptionQuotaExceededException;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import com.email.validator.smart_email_validator.repository.CheckedEmailRepository;
import com.email.validator.smart_email_validator.repository.EmailCheckResultRepository;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailAttemptPersistenceService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final CheckedEmailRepository checkedEmailRepository;
    private final EmailCheckResultRepository emailCheckResultRepository;
    private final CheckTypeRepository checkTypeRepository;

    /**
     * Atomically reserves one email validation and creates its attempt.
     * Both operations are committed in the same transaction.
     */
    @Transactional
    public CheckedEmail reserveAndCreate(
            UUID subscriptionId,
            String email
    ) {
        int updated = subscriptionRepository.reserveOneEmail(
                subscriptionId,
                SubscriptionStatus.ACTIVE,
                Instant.now()
        );

        if (updated != 1) {
            throw new SubscriptionQuotaExceededException(
                    "Subscription is unavailable, inactive, expired, "
                            + "or its email validation quota is exhausted."
            );
        }

        UserSubscription subscription =
                subscriptionRepository.getReferenceById(subscriptionId);

        CheckedEmail attempt = new CheckedEmail();
        attempt.setSubscription(subscription);
        attempt.setEmail(email);
        attempt.setStatus(EmailValidationStatus.IN_PROGRESS);
        attempt.setTotalChecks(0);
        attempt.setCompletedChecks(0);
        attempt.setPassedChecks(0);
        attempt.setFailedChecks(0);
        attempt.setTotalWeight(BigDecimal.ZERO);
        attempt.setEarnedWeight(BigDecimal.ZERO);
        attempt.setScorePercentage(BigDecimal.ZERO);
        attempt.setRiskPercentage(BigDecimal.ZERO);

        return checkedEmailRepository.save(attempt);
    }

    /**
     * Returns only check types enabled in the database.
     */
    @Transactional(readOnly = true)
    public List<CheckType> getEnabledCheckTypes() {
        return checkTypeRepository.findByEnabledTrue();
    }

    /**
     * Persists one successful or ordinary check result.
     */
    @Transactional
    public EmailCheckResult saveCheckResult(EmailCheckResult result) {
        return emailCheckResultRepository.save(result);
    }

    /**
     * Persists an ERROR result when a check throws an exception.
     */
    @Transactional
    public EmailCheckResult saveErrorResult(
            CheckedEmail attempt,
            String checkName,
            String errorMessage
    ) {
        CheckType checkType = checkTypeRepository
                .findByNameIgnoreCase(checkName)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CheckType not found: " + checkName
                ));

        BigDecimal weight = BigDecimal.valueOf(
                checkType.getDefaultWeight()
        );

        EmailCheckResult result = EmailCheckResult.builder()
                .checkedEmail(checkedEmailRepository.getReferenceById(
                        attempt.getId()
                ))
                .checkType(checkType)
                .weightSnapshot(weight)
                .status(CheckResultStatus.ERROR)
                .passed(null)
                .scoreEarned(BigDecimal.ZERO)
                .errorMessage(errorMessage)
                .build();

        return emailCheckResultRepository.save(result);
    }

    /**
     * Updates the final status and aggregate statistics.
     */
    @Transactional
    public void finalizeAttempt(
            UUID checkedEmailId,
            EmailValidationStatus status,
            BigDecimal scorePercentage,
            BigDecimal riskPercentage,
            BigDecimal totalWeight,
            BigDecimal earnedWeight,
            int totalChecks,
            int completedChecks,
            int passedChecks,
            int failedChecks,
            String message
    ) {
        CheckedEmail attempt = checkedEmailRepository.findById(checkedEmailId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "CheckedEmail not found: " + checkedEmailId
                ));

        attempt.setStatus(status);
        attempt.setScorePercentage(scorePercentage);
        attempt.setRiskPercentage(riskPercentage);
        attempt.setTotalWeight(totalWeight);
        attempt.setEarnedWeight(earnedWeight);
        attempt.setTotalChecks(totalChecks);
        attempt.setCompletedChecks(completedChecks);
        attempt.setPassedChecks(passedChecks);
        attempt.setFailedChecks(failedChecks);
        attempt.setFailureReason(message);
    }
}