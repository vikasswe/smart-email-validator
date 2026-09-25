package com.email.validator.smart_email_validator.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "email_record",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_email_record_email",
                        columnNames = "email"
                )
        },
        indexes = {
                @Index(name = "idx_email_record_user", columnList = "user_id"),
                @Index(name = "idx_email_record_domain", columnList = "domain")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(nullable = false, length = 253)
    private String domain;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "emailRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ValidationExecution> validations = new ArrayList<>();
}
