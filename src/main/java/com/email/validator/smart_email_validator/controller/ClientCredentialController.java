package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.response.ClientCredentialResponse;
import com.email.validator.smart_email_validator.service.ClientCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/client-credentials")
@RequiredArgsConstructor
public class ClientCredentialController {

    private final ClientCredentialService credentialService;

    @PostMapping("/users/{userId}/subscriptions/{subscriptionId}/generate")
    public ResponseEntity<ClientCredentialResponse> generate(
            @PathVariable UUID userId,
            @PathVariable UUID subscriptionId
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(credentialService.generate(userId, subscriptionId));
    }

    @GetMapping("/users/{userId}/subscriptions/{subscriptionId}")
    public ResponseEntity<List<ClientCredentialResponse>> getBySubscription(
            @PathVariable UUID userId,
            @PathVariable UUID subscriptionId
    ) {
        return ResponseEntity.ok(
                credentialService.getBySubscription(userId, subscriptionId)
        );
    }

    @GetMapping
    public ResponseEntity<List<ClientCredentialResponse>> getAll() {
        return ResponseEntity.ok(credentialService.getAll());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ClientCredentialResponse>> getByUserId(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(credentialService.getByUserId(userId));
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ResponseEntity<List<ClientCredentialResponse>> getBySubscriptionId(
            @PathVariable UUID subscriptionId
    ) {
        return ResponseEntity.ok(
                credentialService.getBySubscriptionId(subscriptionId)
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ClientCredentialResponse> activate(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(credentialService.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ClientCredentialResponse> deactivate(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(credentialService.deactivate(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        credentialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}