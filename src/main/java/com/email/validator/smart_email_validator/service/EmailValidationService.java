package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.response.EmailValidationResponse;
import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.entity.CheckedEmail;
import com.email.validator.smart_email_validator.entity.EmailCheckResult;
import com.email.validator.smart_email_validator.enums.CheckResultStatus;
import com.email.validator.smart_email_validator.enums.EmailValidationStatus;
import com.email.validator.smart_email_validator.exception.SubscriptionQuotaExceededException;
import com.email.validator.smart_email_validator.runner.EmailCheckRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EmailValidationService {

    private final EmailAttemptPersistenceService persistenceService;
    private final List<EmailCheckRunner> checkRunners;
    private final Executor checkExecutor;

    public EmailValidationService(
            EmailAttemptPersistenceService persistenceService,
            List<EmailCheckRunner> checkRunners,
            @Qualifier("emailCheckExecutor") Executor emailCheckExecutor
    ) {
        this.persistenceService = persistenceService;
        this.checkRunners = checkRunners;
        this.checkExecutor = emailCheckExecutor;
    }

    public EmailValidationResponse validate(
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
                    "Email list must not be empty."
            );
        }

        if (emails.size() > 1000) {
            throw new IllegalArgumentException(
                    "Maximum 1000 emails are allowed per request."
            );
        }

        UUID requestId = UUID.randomUUID();

        List<CheckType> enabledTypes =
                persistenceService.getEnabledCheckTypes();

        Map<String, EmailCheckRunner> runnersByName =
                checkRunners.stream().collect(Collectors.toMap(
                        runner -> normalize(runner.checkName()),
                        Function.identity(),
                        (first, second) -> {
                            throw new IllegalStateException(
                                    "Duplicate check runner: "
                                            + first.checkName()
                            );
                        }
                ));

        List<EmailCheckRunner> enabledRunners =
                enabledTypes.stream()
                        .map(type -> {
                            EmailCheckRunner runner =
                                    runnersByName.get(normalize(type.getName()));

                            if (runner == null) {
                                throw new IllegalStateException(
                                        "Enabled check has no runner: "
                                                + type.getName()
                                );
                            }

                            return runner;
                        })
                        .toList();

        List<EmailValidationResponse.EmailValidationResult> results =
                emails.stream()
                        .map(email -> validateOne(
                                email,
                                subscriptionId,
                                enabledRunners
                        ))
                        .toList();

        int started = (int) results.stream()
                .filter(result -> result.checkedEmailId() != null)
                .count();

        return new EmailValidationResponse(
                requestId,
                emails.size(),
                started,
                emails.size() - started,
                results
        );
    }

    private EmailValidationResponse.EmailValidationResult validateOne(
            String email,
            UUID subscriptionId,
            List<EmailCheckRunner> enabledRunners
    ) {
        if (email == null || email.isBlank() || email.length() > 320) {
            return rejected(
                    email,
                    "Email must be non-blank and at most 320 characters."
            );
        }

        final CheckedEmail attempt;

        try {
            attempt = persistenceService.reserveAndCreate(
                    subscriptionId,
                    email
            );
        } catch (SubscriptionQuotaExceededException ex) {
            return rejected(email, ex.getMessage());
        }

        List<CompletableFuture<CheckExecution>> futures =
                new ArrayList<>();

        for (EmailCheckRunner runner : enabledRunners) {
            try {
                CompletableFuture<CheckExecution> future =
                        CompletableFuture.supplyAsync(
                                () -> executeCheck(runner, attempt),
                                checkExecutor
                        );

                futures.add(future);
            } catch (RejectedExecutionException ex) {
                // The attempt has already been charged.
                // Record the scheduling failure if possible.
                futures.add(CompletableFuture.completedFuture(
                        saveSchedulingError(attempt, runner, ex)
                ));
            }
        }

        List<CheckExecution> executions = futures.stream()
                .map(future -> {
                    try {
                        return future.join();
                    } catch (CompletionException ex) {
                        return CheckExecution.errorOutcome();
                    }
                })
                .toList();

        List<EmailCheckResult> savedResults = executions.stream()
                .map(CheckExecution::result)
                .filter(Objects::nonNull)
                .toList();

        int total = enabledRunners.size();

        int completed = (int) executions.stream()
                .filter(CheckExecution::completed)
                .count();

        int passed = (int) executions.stream()
                .filter(CheckExecution::passed)
                .count();

        int failed = (int) executions.stream()
                .filter(CheckExecution::failed)
                .count();

        boolean hasErrors = executions.stream()
                .anyMatch(CheckExecution::error);

        BigDecimal totalWeight = savedResults.stream()
                .map(EmailCheckResult::getWeightSnapshot)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal earnedWeight = savedResults.stream()
                .filter(result -> Boolean.TRUE.equals(result.getPassed()))
                .map(EmailCheckResult::getScoreEarned)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal score = totalWeight.signum() > 0
                ? earnedWeight.multiply(BigDecimal.valueOf(100))
                .divide(totalWeight, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        score = score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        BigDecimal risk = BigDecimal.valueOf(100)
                .subtract(score)
                .max(BigDecimal.ZERO);

        EmailValidationStatus finalStatus;

        if (hasErrors || completed < total || total == 0) {
            finalStatus = EmailValidationStatus.UNVERIFIABLE;
        } else if (failed == 0) {
            finalStatus = EmailValidationStatus.VALID;
        } else {
            finalStatus = EmailValidationStatus.RISKY;
        }

        String message = hasErrors || completed < total
                ? "One or more checks could not be completed."
                : "Validation checks completed.";

        persistenceService.finalizeAttempt(
                attempt.getId(),
                finalStatus,
                score,
                risk,
                totalWeight,
                earnedWeight,
                total,
                completed,
                passed,
                failed,
                message
        );

        List<EmailValidationResponse.CheckResult> checkResponses =
                savedResults.stream()
                        .map(this::toCheckResponse)
                        .toList();

        return new EmailValidationResponse.EmailValidationResult(
                attempt.getId(),
                email,
                finalStatus.name(),
                score,
                risk,
                total,
                completed,
                passed,
                failed,
                message,
                checkResponses
        );
    }

    private CheckExecution executeCheck(
            EmailCheckRunner runner,
            CheckedEmail attempt
    ) {
        try {
            EmailCheckResult result = runner.run(attempt);

            EmailCheckResult saved =
                    persistenceService.saveCheckResult(result);

            return CheckExecution.from(saved);

        } catch (Exception ex) {
            try {
                EmailCheckResult errorResult =
                        persistenceService.saveErrorResult(
                                attempt,
                                runner.checkName(),
                                safeMessage(ex)
                        );

                return CheckExecution.from(errorResult);

            } catch (Exception persistenceException) {
                return CheckExecution.errorOutcome();
            }
        }
    }

    private CheckExecution saveSchedulingError(
            CheckedEmail attempt,
            EmailCheckRunner runner,
            Exception ex
    ) {
        try {
            EmailCheckResult errorResult =
                    persistenceService.saveErrorResult(
                            attempt,
                            runner.checkName(),
                            "Check could not be scheduled: "
                                    + safeMessage(ex)
                    );

            return CheckExecution.from(errorResult);
        } catch (Exception persistenceException) {
            return CheckExecution.errorOutcome();
        }
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();

        if (message == null || message.isBlank()) {
            return "Check execution failed.";
        }

        return message.length() > 1000
                ? message.substring(0, 1000)
                : message;
    }

    private EmailValidationResponse.CheckResult toCheckResponse(
            EmailCheckResult result
    ) {
        String message = result.getErrorMessage() != null
                ? result.getErrorMessage()
                : result.getResultMessage();

        return new EmailValidationResponse.CheckResult(
                result.getId(),
                result.getCheckType().getName(),
                result.getWeightSnapshot(),
                result.getStatus().name(),
                result.getPassed(),
                result.getScoreEarned(),
                message
        );
    }

    private EmailValidationResponse.EmailValidationResult rejected(
            String email,
            String message
    ) {
        return new EmailValidationResponse.EmailValidationResult(
                null,
                email,
                "REJECTED",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0,
                0,
                0,
                0,
                message,
                List.of()
        );
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private record CheckExecution(
            EmailCheckResult result,
            boolean completed,
            boolean passed,
            boolean failed,
            boolean error
    ) {
        static CheckExecution from(EmailCheckResult result) {
            CheckResultStatus status = result.getStatus();

            return new CheckExecution(
                    result,
                    status == CheckResultStatus.PASSED
                            || status == CheckResultStatus.FAILED
                            || status == CheckResultStatus.SKIPPED
                            || status == CheckResultStatus.ERROR,
                    status == CheckResultStatus.PASSED,
                    status == CheckResultStatus.FAILED,
                    status == CheckResultStatus.ERROR
            );
        }

        static CheckExecution errorOutcome() {
            return new CheckExecution(
                    null,
                    false,
                    false,
                    false,
                    true
            );
        }
    }
}