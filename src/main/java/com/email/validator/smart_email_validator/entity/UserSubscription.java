package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "user_subscription",
        indexes = {
                @Index(name = "idx_user_subscription_user", columnList = "user_id"),
                @Index(name = "idx_user_subscription_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    /*
     * SNAPSHOT VALUES
     *
     * These values never change because somebody
     * edited the plan later.
     */

    @Column(name = "plan_code_snapshot", nullable = false, length = 50)
    private String planCodeSnapshot;

    @Column(name = "plan_name_snapshot", nullable = false, length = 100)
    private String planNameSnapshot;

    @Column(name = "price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle_snapshot", nullable = false, length = 20)
    private com.email.validator.smart_email_validator.enums.BillingCycle billingCycleSnapshot;

    @Column(name = "credits_snapshot", nullable = false)
    private Long creditsSnapshot;

    @Column(name = "max_emails_per_request_snapshot", nullable = false)
    private Integer maxEmailsPerRequestSnapshot;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @OneToMany(
            mappedBy = "subscription",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<UserSubscriptionCheck> checks = new ArrayList<>();
}