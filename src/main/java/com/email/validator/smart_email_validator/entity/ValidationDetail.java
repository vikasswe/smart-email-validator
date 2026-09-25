package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.CheckResult;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(
        name = "validation_detail",
        indexes = {
                @Index(
                        name = "idx_validation_detail_execution",
                        columnList = "validation_execution_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationDetail extends BaseEntity{

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "validation_execution_id",
            nullable = false
    )
    private ValidationExecution validationExecution;

    @Column(name = "check_code", nullable = false, length = 100)
    private String checkCode;

    @Column(name = "check_name", nullable = false, length = 150)
    private String checkName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CheckResult result;

    @Column(nullable = false, length = 1000)
    private String message;

    /**
     * Weight used during this particular validation.
     * <p>
     * This is a snapshot.
     */
    @Column(nullable = false)
    private Integer weight;

    @Column(name = "earned_points", nullable = false)
    private Integer earnedPoints;

}