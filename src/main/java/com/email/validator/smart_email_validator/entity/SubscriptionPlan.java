package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.BillingCycle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "subscription_plan",
        indexes = {
                @Index(
                        name = "idx_subscription_plan_active",
                        columnList = "active"
                ),
                @Index(
                        name = "idx_subscription_plan_priority",
                        columnList = "priority"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            nullable = false,
            length = 100,
            unique = true
    )
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "billing_cycle",
            nullable = false,
            length = 20
    )
    private BillingCycle billingCycle;

    @Column(nullable = false)
    private Long credits;

    @Column(
            name = "max_emails_per_request",
            nullable = false
    )
    private Integer maxEmailsPerRequest;

    @Column(
            name = "email_check_per_minute",
            nullable = false
    )
    private Integer emailCheckPerMinute;

    @Column(
            name = "total_email_check_till_expiry",
            nullable = false
    )
    private Integer totalEmailCheckTillExpiry;

    @Column(nullable = false)
    private Integer priority;

    @Column(
            name = "num_of_client_id_secret_generate",
            nullable = false
    )
    private Integer numOfClientIdSecretGenerate;

    @Column(
            name = "plan_duration_days",
            nullable = false
    )
    private Integer planDurationDays;

    @Column(nullable = false)
    private Boolean active;
}