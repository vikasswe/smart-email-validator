package com.email.validator.smart_email_validator.entity;


import com.email.validator.smart_email_validator.enums.CheckResultStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "email_check_result",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_check_result",
                        columnNames = {"checked_email_id", "check_type_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_email_check_result_email",
                        columnList = "checked_email_id"
                ),
                @Index(
                        name = "idx_email_check_result_type",
                        columnList = "check_type_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailCheckResult extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "checked_email_id", nullable = false)
    private CheckedEmail checkedEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "check_type_id", nullable = false)
    private CheckType checkType;

    @Column(name = "weight_snapshot", nullable = false,
            precision = 12, scale = 4)
    private BigDecimal weightSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CheckResultStatus status;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "score_earned", precision = 12, scale = 4)
    private BigDecimal scoreEarned;

    @Column(name = "result_message", length = 1000)
    private String resultMessage;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;
}