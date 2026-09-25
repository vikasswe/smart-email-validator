package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.response.BlacklistEntryResponse;
import com.email.validator.smart_email_validator.dto.response.BlacklistUploadResponse;
import com.email.validator.smart_email_validator.dto.response.PageResponse;
import com.email.validator.smart_email_validator.entity.BlacklistEntry;
import com.email.validator.smart_email_validator.enums.BlacklistType;
import com.email.validator.smart_email_validator.repository.BlacklistBulkRepository;
import com.email.validator.smart_email_validator.repository.BlacklistEntryRepository;
import com.email.validator.smart_email_validator.utils.BlacklistValueUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final BlacklistEntryRepository blacklistEntryRepository;
    private final BlacklistBulkRepository blacklistBulkRepository;

    @Transactional
    public BlacklistUploadResponse upload(
            MultipartFile file
    ) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Blacklist file is empty"
            );
        }

        String fileName = file.getOriginalFilename();

        long totalLines = 0;
        long blankLines = 0;
        long duplicateLinesInFile = 0;
        long invalidLines = 0;
        long inserted = 0;
        long uniqueRecords = 0;

        Set<String> uniqueValues = new HashSet<>();

        List<BlacklistBulkRepository.BlacklistBatchItem> batch =
                new ArrayList<>(1000);

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                totalLines++;

                String value =
                        BlacklistValueUtils.normalize(line);

                /*
                 * Blank line
                 */
                if (value == null || value.isBlank()) {
                    blankLines++;
                    continue;
                }

                /*
                 * Detect EMAIL / DOMAIN
                 */
                BlacklistType type =
                        BlacklistValueUtils.detectType(value);

                if (type == null) {
                    invalidLines++;
                    continue;
                }

                /*
                 * Prevent duplicate values inside the same file.
                 */
                String uniqueKey =
                        type.name() + ":" + value;

                if (!uniqueValues.add(uniqueKey)) {
                    duplicateLinesInFile++;
                    continue;
                }

                uniqueRecords++;

                batch.add(
                        new BlacklistBulkRepository.BlacklistBatchItem(
                                value,
                                type,
                                "Imported from " + fileName
                        )
                );

                /*
                 * Insert every 1000 records.
                 */
                if (batch.size() >= 1000) {

                    inserted +=
                            blacklistBulkRepository.insertBatch(batch);

                    batch.clear();
                }
            }
        }

        /*
         * Insert remaining records.
         */
        if (!batch.isEmpty()) {

            inserted +=
                    blacklistBulkRepository.insertBatch(batch);

            batch.clear();
        }

        /*
         * Because the file was already deduplicated,
         * anything not inserted was already present in DB.
         */
        long alreadyExists =
                uniqueRecords - inserted;

        return BlacklistUploadResponse.builder()
                .fileName(fileName)
                .totalLines(totalLines)
                .blankLines(blankLines)
                .duplicateLinesInFile(duplicateLinesInFile)
                .invalidLines(invalidLines)
                .alreadyExists(alreadyExists)
                .inserted(inserted)
                .build();
    }

    @Transactional(readOnly = true)
    public BlacklistEntryResponse getByValue(
            String value
    ) {

        String normalized =
                BlacklistValueUtils.normalize(value);

        BlacklistType type =
                BlacklistValueUtils.detectType(normalized);

        if (type == null) {
            throw new IllegalArgumentException(
                    "Invalid blacklist value"
            );
        }

        BlacklistEntry entry =
                blacklistEntryRepository
                        .findByValueAndType(normalized, type)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Blacklist entry not found"
                                )
                        );

        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public PageResponse<BlacklistEntryResponse> search(
            String pattern,
            BlacklistType type,
            int page,
            int size
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 500) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 500"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.ASC,
                        "value"
                )
        );

        Page<BlacklistEntry> result;

        if (pattern != null && !pattern.isBlank()
                && type != null) {

            result =
                    blacklistEntryRepository
                            .findByTypeAndValueContainingIgnoreCase(
                                    type,
                                    pattern.trim(),
                                    pageable
                            );

        } else if (pattern != null && !pattern.isBlank()) {

            result =
                    blacklistEntryRepository
                            .findByValueContainingIgnoreCase(
                                    pattern.trim(),
                                    pageable
                            );

        } else if (type != null) {

            result =
                    blacklistEntryRepository
                            .findByType(
                                    type,
                                    pageable
                            );

        } else {

            result =
                    blacklistEntryRepository.findAll(pageable);
        }

        List<BlacklistEntryResponse> content =
                result.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return PageResponse.<BlacklistEntryResponse>builder()
                .content(content)
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    private BlacklistEntryResponse toResponse(
            BlacklistEntry entry
    ) {

        return BlacklistEntryResponse.builder()
                .id(entry.getId())
                .value(entry.getValue())
                .type(entry.getType())
                .reason(entry.getReason())
                .build();
    }
}