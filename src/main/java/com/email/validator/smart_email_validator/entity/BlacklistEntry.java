package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.BlacklistType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "blacklist_entry",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_blacklist_type_value",
                        columnNames = {"type", "value"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_blacklist_value",
                        columnList = "value"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlacklistEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 320)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlacklistType type;

    @Column(length = 500)
    private String reason;
}