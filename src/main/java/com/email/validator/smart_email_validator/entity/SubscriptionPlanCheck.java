package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(
        name = "subscription_plan_check",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_plan_check",
                columnNames = {
                        "plan_id",
                        "check_type_id"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanCheck extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "plan_id",
            nullable = false
    )
    private SubscriptionPlan plan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "check_type_id",
            nullable = false
    )
    private CheckType checkType;

    @Column(
            name = "default_enabled",
            nullable = false
    )
    private Boolean defaultEnabled;
}