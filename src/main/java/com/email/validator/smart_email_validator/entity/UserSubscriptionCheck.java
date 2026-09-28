package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "user_subscription_check",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_subscription_check",
                columnNames = {
                        "subscription_id",
                        "check_type_id"
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionCheck extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_type_id", nullable = false)
    private CheckType checkType;

    @Column(nullable = false)
    private Boolean enabled;
}