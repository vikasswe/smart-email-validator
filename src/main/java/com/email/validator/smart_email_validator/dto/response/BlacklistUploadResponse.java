package com.email.validator.smart_email_validator.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BlacklistUploadResponse {

    private String fileName;

    private long totalLines;

    private long blankLines;

    private long duplicateLinesInFile;

    private long invalidLines;

    private long alreadyExists;

    private long inserted;

}