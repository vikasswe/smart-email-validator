package com.email.validator.smart_email_validator.dto.response;

import com.email.validator.smart_email_validator.enums.BlacklistType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BlacklistEntryResponse {

    private UUID id;

    private String value;

    private BlacklistType type;

    private String reason;

}