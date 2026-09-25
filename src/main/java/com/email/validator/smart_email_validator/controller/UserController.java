package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.request.UserCreateRequest;
import com.email.validator.smart_email_validator.dto.response.UserResponse;
import com.email.validator.smart_email_validator.service.UserService;
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
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

    private final UserService service;

    @PostMapping
    @Operation(summary = "Create user")
    public ResponseEntity<UserResponse> create(
            @Valid
            @RequestBody
            UserCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by UUID")
    public ResponseEntity<UserResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    @GetMapping("/email")
    @Operation(summary = "Get user by email")
    public ResponseEntity<UserResponse> getByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                service.getByEmail(email)
        );
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserResponse>> getAll() {

        return ResponseEntity.ok(
                service.getAll()
        );
    }
}