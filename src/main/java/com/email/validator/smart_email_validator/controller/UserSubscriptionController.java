package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.request.UserSubscriptionCreateRequest;
import com.email.validator.smart_email_validator.dto.response.UserSubscriptionResponse;
import com.email.validator.smart_email_validator.service.UserSubscriptionService;
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
@RequestMapping("/api/v1/user-subscriptions")
@RequiredArgsConstructor
@Tag(name = "User Subscriptions")
public class UserSubscriptionController {

    private final UserSubscriptionService service;

    @PostMapping
    @Operation(summary = "Subscribe a user to a plan")
    public ResponseEntity<UserSubscriptionResponse> subscribe(
            @Valid @RequestBody UserSubscriptionCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.subscribe(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by UUID")
    public ResponseEntity<UserSubscriptionResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "Get user's active subscription")
    public ResponseEntity<UserSubscriptionResponse> getActiveByUserId(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(
                service.getActiveByUserId(userId)
        );
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get subscriptions by user UUID")
    public ResponseEntity<List<UserSubscriptionResponse>> getByUserId(
            @PathVariable UUID userId) {

        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping
    @Operation(summary = "Get all subscriptions")
    public ResponseEntity<List<UserSubscriptionResponse>> getAll() {

        return ResponseEntity.ok(service.getAll());
    }
}