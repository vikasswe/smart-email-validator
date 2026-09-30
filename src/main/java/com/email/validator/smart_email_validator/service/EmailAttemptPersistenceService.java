
package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.entity.*;
import com.email.validator.smart_email_validator.enums.CheckResultStatus;
import com.email.validator.smart_email_validator.enums.EmailValidationStatus;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.exception.SubscriptionQuotaExceededException;
import com.email.validator.smart_email_validator.repository.CheckedEmailRepository;
import com.email.validator.smart_email_validator.repository.EmailCheckResultRepository;
import com.email.validator.smart_email_validator.repository.UserSubscriptionCheckRepository;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailAttemptPersistenceService {

    private final UserSubscriptionRepository subscriptionRepository;
    private final UserSubscriptionCheckRepository subscriptionCheckRepository;
    private final CheckedEmailRepository checkedEmailRepository;
    private final EmailCheckResultRepository emailCheckResultRepository;

    @Transactional(readOnly = true)
    public Integer getMaxEmailsPerRequest(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(UserSubscription::getMaxEmailsPerRequestSnapshot)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription not found: " + subscriptionId
                ));
    }

    @Transactional(readOnly = true)
    public List<CheckType> getEnabledCheckTypes(UUID subscriptionId) {
        return subscriptionCheckRepository
                .findBySubscriptionId(subscriptionId)
                .stream()
                .filter(check -> Boolean.TRUE.equals(check.getEnabled()))
                .map(UserSubscriptionCheck::getCheckType)
                .filter(checkType ->
                        checkType != null
                                && Boolean.TRUE.equals(checkType.getEnabled()))
                .toList();
    }

    /**
     * Atomically reserves one quota unit and creates the email attempt.
     * Both operations commit or roll back together.
     */
    @Transactional
    public CheckedEmail reserveAndCreate(
            UUID subscriptionId,
            String email
    ) {
        int reserved = subscriptionRepository.reserveOneEmail(
                subscriptionId,
                SubscriptionStatus.ACTIVE,
                Instant.now()
        );

        if (reserved == 0) {
            throw new SubscriptionQuotaExceededException(
                    "Subscription is inactive, expired, or has exhausted its quota."
            );
        }

        UserSubscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Subscription not found: " + subscriptionId
                ));

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
     * Persists a successful or failed check result.
     * The attempt is loaded inside this transaction using its UUID.
     */
    @Transactional
    public EmailCheckResult saveCheckResult(
            UUID attemptId,
            CheckType checkType,
            EmailCheckResult result
    ) {
        CheckedEmail attempt = checkedEmailRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email attempt not found: " + attemptId
                ));

        result.setCheckedEmail(attempt);
        result.setCheckType(checkType);

        if (result.getWeightSnapshot() == null) {
            result.setWeightSnapshot(getDefaultWeight(checkType));
        }

        return emailCheckResultRepository.save(result);
    }

    /**
     * Persists an error result.
     * Accepts the attempt UUID so detached entities are not passed
     * between asynchronous threads.
     */
    @Transactional
    public EmailCheckResult saveErrorResult(
            UUID attemptId,
            CheckType checkType,
            Exception exception
    ) {
        CheckedEmail attempt = checkedEmailRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email attempt not found: " + attemptId
                ));

        EmailCheckResult result = new EmailCheckResult();
        result.setCheckedEmail(attempt);
        result.setCheckType(checkType);
        result.setWeightSnapshot(getDefaultWeight(checkType));
        result.setStatus(CheckResultStatus.ERROR);
        result.setPassed(false);
        result.setScoreEarned(BigDecimal.ZERO);
        result.setResultMessage("Check execution failed.");
        result.setErrorMessage(
                exception.getMessage() == null
                        ? exception.getClass().getSimpleName()
                        : exception.getMessage()
        );

        return emailCheckResultRepository.save(result);
    }

    /**
     * Calculates final counters and score from persisted check results.
     */
    @Transactional
    public CheckedEmail finalizeAttempt(
            UUID attemptId,
            int totalChecks
    ) {
        CheckedEmail attempt = checkedEmailRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Email attempt not found: " + attemptId
                ));

        List<EmailCheckResult> results =
                emailCheckResultRepository.findAllByCheckedEmailId(attemptId);

        BigDecimal totalWeight = BigDecimal.ZERO;
        BigDecimal earnedWeight = BigDecimal.ZERO;

        int completedChecks = 0;
        int passedChecks = 0;
        int failedChecks = 0;

        boolean hasError = false;
        boolean hasSkipped = false;

        for (EmailCheckResult result : results) {
            BigDecimal weight = result.getWeightSnapshot() == null
                    ? BigDecimal.ZERO
                    : result.getWeightSnapshot();

            totalWeight = totalWeight.add(weight);

            CheckResultStatus status = result.getStatus();

            if (status == CheckResultStatus.PASSED
                    || status == CheckResultStatus.FAILED) {
                completedChecks++;
            }

            if (status == CheckResultStatus.PASSED
                    && Boolean.TRUE.equals(result.getPassed())) {
                passedChecks++;

                if (result.getScoreEarned() != null) {
                    earnedWeight = earnedWeight.add(result.getScoreEarned());
                }
            } else if (status == CheckResultStatus.FAILED
                    || (status == CheckResultStatus.PASSED
                    && Boolean.FALSE.equals(result.getPassed()))) {
                failedChecks++;
            } else if (status == CheckResultStatus.ERROR) {
                hasError = true;
            } else if (status == CheckResultStatus.SKIPPED) {
                hasSkipped = true;
            }
        }

        BigDecimal scorePercentage = BigDecimal.ZERO;

        if (totalWeight.signum() > 0) {
            scorePercentage = earnedWeight
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalWeight, 4, RoundingMode.HALF_UP);
        }

        scorePercentage = scorePercentage
                .max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100));

        BigDecimal riskPercentage = BigDecimal.valueOf(100)
                .subtract(scorePercentage)
                .max(BigDecimal.ZERO)
                .min(BigDecimal.valueOf(100));

        attempt.setTotalChecks(totalChecks);
        attempt.setCompletedChecks(completedChecks);
        attempt.setPassedChecks(passedChecks);
        attempt.setFailedChecks(failedChecks);
        attempt.setTotalWeight(totalWeight);
        attempt.setEarnedWeight(earnedWeight);
        attempt.setScorePercentage(scorePercentage);
        attempt.setRiskPercentage(riskPercentage);

        if (totalChecks == 0
                || completedChecks < totalChecks
                || hasError
                || hasSkipped) {
            attempt.setStatus(EmailValidationStatus.UNVERIFIABLE);
        } else if (failedChecks == 0) {
            attempt.setStatus(EmailValidationStatus.VALID);
        } else {
            attempt.setStatus(EmailValidationStatus.RISKY);
        }

        return checkedEmailRepository.save(attempt);
    }

    private BigDecimal getDefaultWeight(CheckType checkType) {
        return checkType.getDefaultWeight() == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(checkType.getDefaultWeight());
    }
}