package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.request.CheckTypeCreateRequest;
import com.email.validator.smart_email_validator.dto.response.CheckTypeResponse;
import com.email.validator.smart_email_validator.service.CheckTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/checks")
@RequiredArgsConstructor
@Tag(name = "Check Type Master")
public class CheckTypeController {

    private final CheckTypeService service;

    @PostMapping
    @Operation(summary = "Create check type")
    public ResponseEntity<CheckTypeResponse> create(
            @Valid @RequestBody CheckTypeCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get check type by UUID")
    public ResponseEntity<CheckTypeResponse> getById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping
    @Operation(summary = "Get all check types")
    public ResponseEntity<Page<CheckTypeResponse>> getAll(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        return ResponseEntity.ok(
                service.getAll(name, pageable)
        );
    }
}