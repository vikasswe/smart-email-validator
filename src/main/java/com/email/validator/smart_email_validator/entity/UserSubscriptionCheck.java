package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "user_subscription_check",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_subscription_check",
                        columnNames = {"subscription_id", "check_type_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_user_subscription_check_subscription",
                        columnList = "subscription_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionCheck {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_type_id", nullable = false)
    private CheckType checkType;

    /*
     * Snapshot values.
     */

    @Column(name = "check_code_snapshot", nullable = false, length = 100)
    private String checkCodeSnapshot;

    @Column(name = "check_name_snapshot", nullable = false, length = 150)
    private String checkNameSnapshot;

    @Column(name = "weight_snapshot", nullable = false)
    private Integer weightSnapshot;

    @Column(name = "estimated_time_ms_snapshot", nullable = false)
    private Long estimatedTimeMsSnapshot;

    /**
     * Subscription plan grants this check.
     */
    @Column(nullable = false)
    private Boolean allowed;

    /**
     * User has selected whether this check
     * should execute for their account.
     */
    @Column(nullable = false)
    private Boolean enabled;
}