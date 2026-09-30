package com.email.validator.smart_email_validator.security;

import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import com.email.validator.smart_email_validator.utils.SubscriptionRequestAttributes;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Component
@Order(2)
@RequiredArgsConstructor
public class SubscriptionValidationFilter extends OncePerRequestFilter {

    private final UserSubscriptionRepository subscriptionRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().equals("/api/v1/validate");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Object value = request.getAttribute(
                SubscriptionRequestAttributes.SUBSCRIPTION_ID
        );

        if (!(value instanceof UUID subscriptionId)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Client credentials were not validated."
            );
            return;
        }

        UserSubscription subscription = subscriptionRepository
                .findById(subscriptionId)
                .orElse(null);

        if (subscription == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Subscription not found."
            );
            return;
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Subscription is not active."
            );
            return;
        }

        Instant now = Instant.now();

        if (subscription.getEndAt() != null
                && !subscription.getEndAt().isAfter(now)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Subscription has expired."
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}