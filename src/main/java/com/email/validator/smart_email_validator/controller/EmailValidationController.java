package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.response.EmailValidationResponse;
import com.email.validator.smart_email_validator.service.EmailValidationService;
import com.email.validator.smart_email_validator.utils.SubscriptionRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmailValidationController {

    private final EmailValidationService validationService;

    @PostMapping("/validate")
    public ResponseEntity<EmailValidationResponse> validate(
            HttpServletRequest request,
            @RequestBody List<String> emails
    ) {
        UUID subscriptionId = (UUID) request.getAttribute(
                SubscriptionRequestAttributes.SUBSCRIPTION_ID
        );

        EmailValidationResponse response =
                validationService.validate(subscriptionId, emails);

        return ResponseEntity.ok(response);
    }
}