package com.email.validator.smart_email_validator.controller;

import com.email.validator.smart_email_validator.dto.response.BlacklistEntryResponse;
import com.email.validator.smart_email_validator.dto.response.BlacklistUploadResponse;
import com.email.validator.smart_email_validator.dto.response.PageResponse;
import com.email.validator.smart_email_validator.enums.BlacklistType;
import com.email.validator.smart_email_validator.service.BlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Tag(
        name = "Blacklist",
        description = "Manage blocked email addresses and domains"
)
@RestController
@RequestMapping("/api/v1/admin/blacklist")
@RequiredArgsConstructor
public class BlacklistController {

    private final BlacklistService blacklistService;

    @Operation(
            summary = "Upload blacklist file",
            description = """
                    Upload a TXT file containing email addresses
                    and domains.

                    The service normalizes values, removes duplicates,
                    detects EMAIL/DOMAIN type and inserts only new records.
                    """
    )
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<BlacklistUploadResponse> upload(
            @RequestParam("file")
            MultipartFile file
    ) throws IOException {

        return ResponseEntity.ok(
                blacklistService.upload(file)
        );
    }

    @Operation(
            summary = "Find blacklist entry",
            description = "Find an exact blacklist entry by email or domain."
    )
    @GetMapping("/value/{value}")
    public ResponseEntity<BlacklistEntryResponse> getByValue(
            @PathVariable String value
    ) {

        return ResponseEntity.ok(
                blacklistService.getByValue(value)
        );
    }

    @Operation(
            summary = "Search blacklist",
            description = """
                    Search blacklist entries using pattern,
                    type and pagination.
                    """
    )
    @GetMapping("/search")
    public ResponseEntity<PageResponse<BlacklistEntryResponse>> search(
            @RequestParam(required = false) String pattern,
            @RequestParam(required = false) BlacklistType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {

        return ResponseEntity.ok(
                blacklistService.search(
                        pattern,
                        type,
                        page,
                        size
                )
        );
    }
}