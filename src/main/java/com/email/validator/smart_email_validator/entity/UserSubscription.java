package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.BillingCycle;
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
                @Index(
                        name = "idx_user_subscription_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_user_subscription_status",
                        columnList = "status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscription extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Column(name = "price_snapshot", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle_snapshot", nullable = false, length = 20)
    private BillingCycle billingCycleSnapshot;

    @Column(name = "credits_snapshot", nullable = false)
    private Long creditsSnapshot;

    @Column(name = "max_emails_per_request_snapshot", nullable = false)
    private Integer maxEmailsPerRequestSnapshot;

    @Column(name = "email_check_per_minute_snapshot", nullable = false)
    private Integer emailCheckPerMinuteSnapshot;

    @Column(name = "total_email_check_till_expiry_snapshot", nullable = false)
    private Integer totalEmailCheckTillExpirySnapshot;

    @Column(name = "num_of_client_id_secret_generate_snapshot", nullable = false)
    private Integer numOfClientIdSecretGenerateSnapshot;

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