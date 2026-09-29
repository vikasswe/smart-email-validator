package com.email.validator.smart_email_validator.security;

import com.email.validator.smart_email_validator.entity.UserSubscription;
import com.email.validator.smart_email_validator.enums.SubscriptionStatus;
import com.email.validator.smart_email_validator.repository.UserSubscriptionRepository;
import com.email.validator.smart_email_validator.utils.SubscriptionRequestAttributes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.List;
import java.util.UUID;

@Component
@Order(2)
@RequiredArgsConstructor
public class SubscriptionValidationFilter extends OncePerRequestFilter {

    private final UserSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

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

        // Step 1: Load the associated subscription.
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

        // Step 2: Check subscription status.
        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Subscription is not active."
            );
            return;
        }

        // Step 3: Check subscription expiry.
        Instant now = Instant.now();

        if (subscription.getEndAt() != null
                && !subscription.getEndAt().isAfter(now)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Subscription has expired."
            );
            return;
        }

        // Step 4: Parse the email request.
        List<String> emails;

        try {
            emails = objectMapper.readValue(
                    request.getInputStream(),
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (JsonProcessingException e) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Request body must be a JSON array of strings."
            );
            return;
        }

        if (emails == null || emails.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "At least one email is required."
            );
            return;
        }

        if (emails.size() > 100) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Maximum 100 emails are allowed per request."
            );
            return;
        }

        if (emails.stream().anyMatch(
                email -> email == null || email.isBlank() || email.length() > 320
        )) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Each email must be a non-empty string of at most 320 characters."
            );
            return;
        }

        Integer maxEmails = subscription.getMaxEmailsPerRequestSnapshot();

        if (maxEmails == null || emails.size() > maxEmails) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Email count exceeds the plan's per-request limit."
            );
            return;
        }

        request.setAttribute("EMAILS", emails);

        boolean exhausted = subscriptionRepository
                .isQuotaExhausted(
                        subscriptionId,
                        SubscriptionStatus.ACTIVE,
                        Instant.now()
                )
                .orElse(true);

        if (exhausted) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                      "status": 429,
                      "error": "QUOTA_EXHAUSTED",
                      "message": "Your subscription has exhausted its validation quota."
                    }
                    """);
            return;
        }

        filterChain.doFilter(request, response);

    }
}