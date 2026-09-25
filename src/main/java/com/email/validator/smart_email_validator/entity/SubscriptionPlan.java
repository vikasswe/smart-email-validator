package com.email.validator.smart_email_validator.entity;

import com.email.validator.smart_email_validator.enums.BillingCycle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "subscription_plan",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subscription_plan_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false, length = 20)
    private BillingCycle billingCycle;

    /**
     * Number of email validation credits.
     */
    @Column(nullable = false)
    private Long credits;

    /**
     * Maximum number of emails accepted in one API request.
     */
    @Column(name = "max_emails_per_request", nullable = false)
    private Integer maxEmailsPerRequest;

    @Column(nullable = false)
    private Boolean active;

}