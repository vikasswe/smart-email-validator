package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.EmailValidationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "checked_email",
        indexes = {
                @Index(name = "idx_checked_email_email", columnList = "email"),
                @Index(name = "idx_checked_email_subscription", columnList = "subscription_id"),
                @Index(name = "idx_checked_email_created", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckedEmail extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription;

    @Column(nullable = false, length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailValidationStatus status;

    @Column(name = "total_checks")
    private Integer totalChecks;

    @Column(name = "completed_checks")
    private Integer completedChecks;

    @Column(name = "passed_checks")
    private Integer passedChecks;

    @Column(name = "failed_checks")
    private Integer failedChecks;

    @Column(name = "total_weight", precision = 12, scale = 4)
    private BigDecimal totalWeight;

    @Column(name = "earned_weight", precision = 12, scale = 4)
    private BigDecimal earnedWeight;

    @Column(name = "score_percentage", precision = 7, scale = 4)
    private BigDecimal scorePercentage;

    @Column(name = "risk_percentage", precision = 7, scale = 4)
    private BigDecimal riskPercentage;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @OneToMany(
            mappedBy = "checkedEmail",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<EmailCheckResult> checkResults = new ArrayList<>();
}