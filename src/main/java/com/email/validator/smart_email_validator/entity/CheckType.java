package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "check_type",
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
public class CheckType extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            nullable = false,
            length = 150,
            unique = true
    )
    private String name;

    @Column(name = "default_weight", nullable = false)
    private Integer defaultWeight;

    @Column(name = "estimated_time_ms", nullable = false)
    private Long estimatedTimeMs;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(
            name = "execution_order",
            nullable = false,
            unique = true
    )
    private Integer executionOrder;
}