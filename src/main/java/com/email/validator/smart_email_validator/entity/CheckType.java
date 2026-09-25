package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "check_type",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_check_type_code",
                        columnNames = "code"
                )
        },
        indexes = {
                @Index(name = "idx_check_type_enabled", columnList = "enabled"),
                @Index(name = "idx_check_type_execution_order", columnList = "execution_order")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckType {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Examples:
     * FORMAT
     * DNS
     * BLACKLIST
     * SMTP
     * REPUTATION
     * SECURITY
     */
    @Column(nullable = false, length = 100)
    private String category;

    /**
     * Default weight for this check.
     */
    @Column(nullable = false)
    private Integer defaultWeight;

    /**
     * Approximate execution time in milliseconds.
     *
     * Example:
     * 5    = local regex
     * 100  = database lookup
     * 1000 = DNS
     * 5000 = SMTP
     */
    @Column(nullable = false)
    private Long estimatedTimeMs;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder;

}