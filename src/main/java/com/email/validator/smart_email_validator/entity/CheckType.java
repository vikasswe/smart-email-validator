package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "check_type",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_check_type_name",
                        columnNames = "name"
                )
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

    @Column(nullable = false, length = 150, unique = true)
    private String name;

    @Column(name = "default_weight", nullable = false)
    private Integer defaultWeight;

    @Column(name = "estimated_time_ms", nullable = false)
    private Long estimatedTimeMs;

    @Column(nullable = false)
    private Boolean enabled;
}