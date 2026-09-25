package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.request.CheckTypeCreateRequest;
import com.email.validator.smart_email_validator.dto.response.CheckTypeResponse;
import com.email.validator.smart_email_validator.entity.CheckType;
import com.email.validator.smart_email_validator.repository.CheckTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckTypeService {

    private final CheckTypeRepository repository;

    public CheckTypeResponse create(CheckTypeCreateRequest request) {

        CheckType checkType = CheckType.builder()
                .name(request.name().trim())
                .defaultWeight(request.defaultWeight())
                .estimatedTimeMs(request.estimatedTimeMs())
                .enabled(request.enabled())
                .executionOrder(request.executionOrder())
                .build();

        return toResponse(repository.save(checkType));
    }

    @Transactional(readOnly = true)
    public CheckTypeResponse getById(UUID id) {

        CheckType checkType = repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Check type not found: " + id));

        return toResponse(checkType);
    }

    @Transactional(readOnly = true)
    public Page<CheckTypeResponse> getAll(
            String name,
            Pageable pageable) {

        Page<CheckType> page;

        if (name == null || name.isBlank()) {
            page = repository.findAll(pageable);
        } else {
            page = repository.findByNameContainingIgnoreCase(
                    name.trim(),
                    pageable
            );
        }

        return page.map(this::toResponse);
    }

    private CheckTypeResponse toResponse(CheckType entity) {

        return CheckTypeResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .defaultWeight(entity.getDefaultWeight())
                .estimatedTimeMs(entity.getEstimatedTimeMs())
                .enabled(entity.getEnabled())
                .executionOrder(entity.getExecutionOrder())
                .build();
    }
}