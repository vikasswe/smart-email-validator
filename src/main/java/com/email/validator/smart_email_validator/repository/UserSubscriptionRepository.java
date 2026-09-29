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

    @Modifying
    @Query("""
            update UserSubscription s
               set s.totalEmailCheckTillNow =
                   coalesce(s.totalEmailCheckTillNow, 0) + :count
             where s.id = :subscriptionId
               and s.status = :status
               and (s.endAt is null or s.endAt > :now)
               and coalesce(s.totalEmailCheckTillNow, 0) + :count
                   <= s.totalEmailCheckTillExpirySnapshot
            """)
    int incrementUsageIfAvailable(
            UUID subscriptionId,
            int count,
            SubscriptionStatus status,
            Instant now
    );


    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query("""
    update UserSubscription s
       set s.totalEmailCheckTillNow =
           coalesce(s.totalEmailCheckTillNow, 0) + 1
     where s.id = :subscriptionId
       and s.status = :status
       and (s.endAt is null or s.endAt > :now)
       and coalesce(s.totalEmailCheckTillNow, 0) + 1
           <= s.totalEmailCheckTillExpirySnapshot
    """)
    int reserveOneEmail(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("status") SubscriptionStatus status,
            @Param("now") Instant now
    );

    @Query("""
        select case when
            coalesce(s.totalEmailCheckTillNow, 0)
                >= s.totalEmailCheckTillExpirySnapshot
            then true else false end
        from UserSubscription s
        where s.id = :subscriptionId
          and s.status = :status
          and (s.endAt is null or s.endAt > :now)
        """)
    Optional<Boolean> isQuotaExhausted(
            @Param("subscriptionId") UUID subscriptionId,
            @Param("status") SubscriptionStatus status,
            @Param("now") Instant now
    );

}