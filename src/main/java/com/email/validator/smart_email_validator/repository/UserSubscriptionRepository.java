package com.email.validator.smart_email_validator.repository;

import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository
        extends JpaRepository<UserSubscription, UUID> {

    boolean existsByUserIdAndStatus(
            UUID userId,
            SubscriptionStatus status
    );

    Optional<UserSubscription> findFirstByUserIdAndStatusOrderByStartAtDesc(
            UUID userId,
            SubscriptionStatus status
    );

    Optional<UserSubscription>
    findFirstByUserIdAndPlanIdAndStatusOrderByStartAtDesc(
            UUID userId,
            UUID planId,
            SubscriptionStatus status
    );

    List<UserSubscription> findByUserId(UUID userId);

    List<UserSubscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Increments usage by the requested count if sufficient quota exists.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE UserSubscription s
               SET s.totalEmailCheckTillNow =
                   COALESCE(s.totalEmailCheckTillNow, 0) + :count
             WHERE s.id = :subscriptionId
               AND s.status = :status
               AND (s.endAt IS NULL OR s.endAt > :now)
               AND COALESCE(s.totalEmailCheckTillNow, 0) + :count
                   <= s.totalEmailCheckTillExpirySnapshot
            """)
    int incrementUsageIfAvailable(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("count") int count,
            @Param("status") SubscriptionStatus status,
            @Param("now") Instant now
    );

    /**
     * Atomically reserves quota for one email attempt.
     * <p>
     * Returns 1 if reserved, otherwise 0.
     */
    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
            UPDATE UserSubscription s
               SET s.totalEmailCheckTillNow =
                   COALESCE(s.totalEmailCheckTillNow, 0) + 1
             WHERE s.id = :subscriptionId
               AND s.status = :status
               AND (s.endAt IS NULL OR s.endAt > :now)
               AND COALESCE(s.totalEmailCheckTillNow, 0) + 1
                   <= s.totalEmailCheckTillExpirySnapshot
            """)
    int reserveOneEmail(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("status") SubscriptionStatus status,
            @Param("now") Instant now
    );

    /**
     * Checks whether the subscription has exhausted its quota.
     * <p>
     * Informational only; do not use this as the authoritative
     * concurrency-safe quota check before reserving an attempt.
     */
    @Query("""
            SELECT CASE WHEN
                COALESCE(s.totalEmailCheckTillNow, 0)
                    >= s.totalEmailCheckTillExpirySnapshot
                THEN TRUE ELSE FALSE END
            FROM UserSubscription s
            WHERE s.id = :subscriptionId
              AND s.status = :status
              AND (s.endAt IS NULL OR s.endAt > :now)
            """)
    Optional<Boolean> isQuotaExhausted(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("status") SubscriptionStatus status,
            @Param("now") Instant now
    );
}