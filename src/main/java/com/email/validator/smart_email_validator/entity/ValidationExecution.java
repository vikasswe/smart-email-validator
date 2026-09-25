package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.EmailStatus;
import com.email.validator.smart_email_validator.enums.ScoreLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "validation_execution",
        indexes = {
                @Index(
                        name = "idx_validation_execution_email",
                        columnList = "email_record_id"
                ),
                @Index(
                        name = "idx_validation_execution_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_validation_execution_started_at",
                        columnList = "started_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationExecution {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "email_record_id", nullable = false)
    private EmailRecord emailRecord;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    @Column(name = "total_checks", nullable = false)
    private Integer totalChecks;

    @Column(name = "applied_checks", nullable = false)
    private Integer appliedChecks;

    @Column(name = "passed_checks", nullable = false)
    private Integer passedChecks;

    @Column(name = "failed_checks", nullable = false)
    private Integer failedChecks;

    @Column(name = "warning_checks", nullable = false)
    private Integer warningChecks;

    @Column(name = "unverifiable_checks", nullable = false)
    private Integer unverifiableChecks;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "risk_percentage", nullable = false)
    private Integer riskPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "score_level", nullable = false, length = 30)
    private ScoreLevel scoreLevel;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @OneToMany(
            mappedBy = "validationExecution",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ValidationDetail> details = new ArrayList<>();
}