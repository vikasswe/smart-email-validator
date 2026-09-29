package com.email.validator.smart_email_validator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "client_credential",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_client_credential_client_id",
                        columnNames = "client_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_client_credential_subscription",
                        columnList = "subscription_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCredential extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private UserSubscription subscription;

    @Column(name = "client_id", nullable = false, unique = true, length = 100)
    private String clientId;

    @Column(name = "client_secret_hash", nullable = false, length = 64)
    private String clientSecretHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(length = 500)
    private String remarks;
}