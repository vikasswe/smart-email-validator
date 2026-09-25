package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.request.SubscriptionPlanCreateRequest;
import com.email.validator.smart_email_validator.dto.response.SubscriptionPlanResponse;
import com.email.validator.smart_email_validator.service.SubscriptionPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "Subscription Plan Master")
public class SubscriptionPlanController {

    private final SubscriptionPlanService service;

    @PostMapping
    @Operation(summary = "Create subscription plan")
    public ResponseEntity<SubscriptionPlanResponse> create(
            @Valid
            @RequestBody
            SubscriptionPlanCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription plan by UUID")
    public ResponseEntity<SubscriptionPlanResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @GetMapping
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }
}