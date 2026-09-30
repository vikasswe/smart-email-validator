package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.response.EmailValidationResponse;
import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.exception.SubscriptionQuotaExceededException;
import com.email.validator.smart_email_validator.repository.EmailCheckResultRepository;
import com.email.validator.smart_email_validator.runner.EmailCheckRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class EmailValidationService {

    private static final int ABSOLUTE_MAX_EMAILS_PER_REQUEST = 100;

    private final EmailAttemptPersistenceService persistenceService;
    private final EmailCheckResultRepository emailCheckResultRepository;
    private final List<EmailCheckRunner> runners;
    private final Executor emailCheckExecutor;

    public EmailValidationService(
            EmailAttemptPersistenceService persistenceService,
            EmailCheckResultRepository emailCheckResultRepository,
            List<EmailCheckRunner> runners,
            @Qualifier("emailCheckExecutor") Executor emailCheckExecutor
    ) {
        this.persistenceService = persistenceService;
        this.emailCheckResultRepository = emailCheckResultRepository;
        this.runners = runners;
        this.emailCheckExecutor = emailCheckExecutor;
    }

    public EmailValidationResponse validate(
            UUID subscriptionId,
            List<String> emails
    ) {
        validateRequest(subscriptionId, emails);

        Integer maxEmails =
                persistenceService.getMaxEmailsPerRequest(subscriptionId);

        if (maxEmails == null
                || maxEmails <= 0
                || emails.size() > maxEmails) {
            throw new IllegalArgumentException(
                    "Email count exceeds the plan's per-request limit."
            );
        }

        List<CheckType> enabledCheckTypes =
                persistenceService.getEnabledCheckTypes(subscriptionId);

        Map<String, EmailCheckRunner> runnerByName = new HashMap<>();

        for (EmailCheckRunner runner : runners) {
            if (runner.checkName() != null) {
                runnerByName.put(runner.checkName(), runner);
            }
        }

        UUID requestId = UUID.randomUUID();

        List<EmailValidationResponse.EmailValidationResult> results =
                new ArrayList<>();

        int startedEmails = 0;
        int rejectedEmails = 0;

        // Emails are intentionally processed sequentially.
        for (String rawEmail : emails) {
            String email = rawEmail == null ? null : rawEmail.trim();

            if (email == null || email.isBlank()) {
                rejectedEmails++;

                results.add(new EmailValidationResponse.EmailValidationResult(
                        null,
                        rawEmail,
                        "REJECTED",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        enabledCheckTypes.size(),
                        0,
                        0,
                        0,
                        "Email address must not be blank.",
                        List.of()
                ));
                continue;
            }

            CheckedEmail attempt;

            try {
                attempt = persistenceService.reserveAndCreate(
                        subscriptionId,
                        email
                );
            } catch (SubscriptionQuotaExceededException exception) {
                rejectedEmails++;

                results.add(new EmailValidationResponse.EmailValidationResult(
                        null,
                        email,
                        "REJECTED",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        enabledCheckTypes.size(),
                        0,
                        0,
                        0,
                        exception.getMessage(),
                        List.of()
                ));
                continue;
            }

            startedEmails++;

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (CheckType checkType : enabledCheckTypes) {
                EmailCheckRunner runner =
                        runnerByName.get(checkType.getName());

                if (runner == null) {
                    persistErrorSafely(
                            attempt,
                            checkType,
                            new IllegalStateException(
                                    "No runner registered for check: "
                                            + checkType.getName()
                            )
                    );
                    continue;
                }

                try {
                    CompletableFuture<Void> future =
                            CompletableFuture.runAsync(() -> {
                                try {
                                    EmailCheckResult result =
                                            runner.run(attempt);

                                    if (result == null) {
                                        throw new IllegalStateException(
                                                "Runner returned null result: "
                                                        + checkType.getName()
                                        );
                                    }

                                    persistenceService.saveCheckResult(
                                            attempt.getId(),
                                            checkType,
                                            result
                                    );
                                } catch (Exception exception) {
                                    log.error(
                                            "Check failed. email={}, check={}",
                                            attempt.getEmail(),
                                            checkType.getName(),
                                            exception
                                    );

                                    persistErrorSafely(
                                            attempt,
                                            checkType,
                                            exception
                                    );
                                }
                            }, emailCheckExecutor);

                    futures.add(future);
                } catch (RuntimeException exception) {
                    log.error(
                            "Could not submit check. email={}, check={}",
                            email,
                            checkType.getName(),
                            exception
                    );

                    persistErrorSafely(
                            attempt,
                            checkType,
                            exception
                    );
                }
            }

            // Wait for every submitted check before finalizing this email.
            for (CompletableFuture<Void> future : futures) {
                try {
                    future.join();
                } catch (CompletionException exception) {
                    log.error(
                            "Unexpected asynchronous failure. email={}",
                            email,
                            exception
                    );
                }
            }

            CheckedEmail finalized = persistenceService.finalizeAttempt(
                    attempt.getId(),
                    enabledCheckTypes.size()
            );

            List<EmailCheckResult> savedResults =
                    emailCheckResultRepository.findAllByCheckedEmailId(
                            finalized.getId()
                    );

            List<EmailValidationResponse.CheckResult> checkResponses =
                    savedResults.stream()
                            .map(this::toCheckResponse)
                            .toList();

            results.add(new EmailValidationResponse.EmailValidationResult(
                    finalized.getId(),
                    finalized.getEmail(),
                    finalized.getStatus().name(),
                    finalized.getScorePercentage(),
                    finalized.getRiskPercentage(),
                    finalized.getTotalChecks(),
                    finalized.getCompletedChecks(),
                    finalized.getPassedChecks(),
                    finalized.getFailedChecks(),
                    buildMessage(finalized),
                    checkResponses
            ));
        }

        return new EmailValidationResponse(
                requestId,
                emails.size(),
                startedEmails,
                rejectedEmails,
                results
        );
    }

    private void validateRequest(
            UUID subscriptionId,
            List<String> emails
    ) {
        if (subscriptionId == null) {
            throw new IllegalArgumentException(
                    "Subscription ID is required."
            );
        }

        if (emails == null || emails.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one email address is required."
            );
        }

        if (emails.size() > ABSOLUTE_MAX_EMAILS_PER_REQUEST) {
            throw new IllegalArgumentException(
                    "Maximum " + ABSOLUTE_MAX_EMAILS_PER_REQUEST
                            + " emails are allowed per request."
            );
        }
    }

    private void persistErrorSafely(
            CheckedEmail attempt,
            CheckType checkType,
            Exception exception
    ) {
        try {
            persistenceService.saveErrorResult(
                    attempt.getId(),
                    checkType,
                    exception
            );
        } catch (Exception persistenceException) {
            log.error(
                    "Could not persist error result. email={}, check={}",
                    attempt.getEmail(),
                    checkType.getName(),
                    persistenceException
            );
        }
    }

    private EmailValidationResponse.CheckResult toCheckResponse(
            EmailCheckResult result
    ) {
        return new EmailValidationResponse.CheckResult(
                result.getId(),
                result.getCheckType().getName(),
                result.getWeightSnapshot(),
                result.getStatus().name(),
                result.getPassed(),
                result.getScoreEarned(),
                result.getResultMessage()
        );
    }

    private String buildMessage(CheckedEmail attempt) {
        if (attempt.getStatus() == null) {
            return "Email validation status is unavailable.";
        }

        return switch (attempt.getStatus()) {
            case VALID -> "All enabled checks passed.";
            case RISKY -> "One or more enabled checks failed.";
            case INVALID -> "The email address is invalid.";
            case UNVERIFIABLE -> "The email could not be fully verified.";
            case FAILED -> "Email validation failed.";
            case IN_PROGRESS -> "Email validation is still in progress.";
            default -> "Email validation completed with status: "
                    + attempt.getStatus().name() + ".";
        };
    }
}