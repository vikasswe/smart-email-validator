package com.email.validator.smart_email_validator.security;

import com.email.validator.smart_email_validator.entity.ClientCredential;
import com.email.validator.smart_email_validator.repository.ClientCredentialRepository;
import com.email.validator.smart_email_validator.utils.CredentialHashUtil;
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

@Component
@Order(1)
@RequiredArgsConstructor
public class ClientCredentialFilter extends OncePerRequestFilter {

    private final ClientCredentialRepository credentialRepository;

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

        String clientId = request.getHeader("X-Client-Id");
        String clientSecret = request.getHeader("X-Client-Secret");

        if (clientId == null || clientId.isBlank()
                || clientSecret == null || clientSecret.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Client ID and client secret are required."
            );
            return;
        }

        // Step 1: Query only the credential table.
        ClientCredential credential = credentialRepository
                .findByClientId(clientId)
                .orElse(null);

        if (credential == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid client credentials."
            );
            return;
        }

        // Step 2: Check whether the credential is active.
        if (!Boolean.TRUE.equals(credential.getActive())) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Client credential is inactive."
            );
            return;
        }

        // Step 3: Verify the client secret.
        if (!CredentialHashUtil.matches(
                clientSecret,
                credential.getClientSecretHash()
        )) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid client credentials."
            );
            return;
        }

        // Step 4: Only after successful credential validation,
        // pass the subscription ID to the next filter.
        request.setAttribute(
                SubscriptionRequestAttributes.SUBSCRIPTION_ID,
                credential.getSubscription().getId()
        );

        filterChain.doFilter(request, response);
    }
}