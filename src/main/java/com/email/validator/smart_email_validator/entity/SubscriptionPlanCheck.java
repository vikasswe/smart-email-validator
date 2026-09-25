package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "subscription_plan_check",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_plan_check",
                        columnNames = {"plan_id", "check_type_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanCheck {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_type_id", nullable = false)
    private CheckType checkType;

    /**
     * Whether this subscription plan gives access
     * to this check.
     */
    @Column(nullable = false)
    private Boolean allowed;

    /**
     * Whether the check is enabled by default
     * when a user subscribes.
     */
    @Column(name = "default_enabled", nullable = false)
    private Boolean defaultEnabled;

    /**
     * Plan-specific weight.
     *
     * This allows BASIC and PREMIUM to assign
     * different weights to the same check.
     */
    @Column(nullable = false)
    private Integer weight;

    /**
     * Plan-specific approximate execution time.
     */
    @Column(name = "estimated_time_ms", nullable = false)
    private Long estimatedTimeMs;

}