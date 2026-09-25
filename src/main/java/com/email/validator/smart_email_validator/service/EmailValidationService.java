package com.email.validator.smart_email_validator.service;

import com.email.validator.smart_email_validator.dto.EmailResponse;
import com.email.validator.smart_email_validator.dto.ValidationDetailDto;
import com.email.validator.smart_email_validator.entity.EmailRecord;
import com.email.validator.smart_email_validator.entity.ValidationDetail;
import com.email.validator.smart_email_validator.repository.EmailRecordRepository;
import com.email.validator.smart_email_validator.strategy.EmailValidationStrategy;
import com.email.validator.smart_email_validator.utils.EmailUtils;
import com.email.validator.smart_email_validator.utils.StatusDecide;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class EmailValidationService {

    private final EmailRecordRepository emailRecordRepository;
    private final List<EmailValidationStrategy> emailValidationStrategies;

    public EmailValidationService(
            EmailRecordRepository emailRecordRepository,
            List<EmailValidationStrategy> emailValidationStrategies) {

        this.emailRecordRepository = emailRecordRepository;
        this.emailValidationStrategies = emailValidationStrategies;
    }

    public EmailResponse validate(String email) {

//        Optional<EmailRecord> existing = emailRecordRepository.findByEmail(email);
//
//        if (existing.isPresent()) {
//            return map(existing.get());
//        }
//
//        EmailRecord emailRecord = new EmailRecord();
//        String domain = EmailUtils.extractDomain(email);
//
//        emailRecord.setEmail(email);
//        emailRecord.setDomain(domain);
//        emailRecord.setCreatedAt(Instant.now());
//
//        List<ValidationDetail> validationDetails = new ArrayList<>();
//
//        boolean isValid = true;
//        double score = 1.0;
//        boolean isDisposable = false;
//        boolean isSpam = false;
//
//        for (EmailValidationStrategy strategy : emailValidationStrategies) {
//            try {
//                ValidationDetail detail = strategy.validate(email);
//                System.out.println(detail.toString());
//
//                detail.setEmailRecord(emailRecord);
//                validationDetails.add(detail);
//
//                if (!detail.getIsPassed()) {
//                    isValid = false;
//                    score -= detail.getImpactScore();
//                    if ("Disposable Check".equals(detail.getCheckName())) {
//                        isDisposable = true;
//                    } else {
//                        isSpam = true;
//                    }
//                }
//
//            } catch (Exception e) {
//
//                ValidationDetail error = new ValidationDetail();
//                error.setCheckName("System Error");
//                error.setIsPassed(false);
//                error.setMessage("Validator failed internally");
//                error.setImpactScore(1.0);
//                error.setEmailRecord(emailRecord);
//
//                validationDetails.add(error);
//
//                isValid = false;
//                score -= 1.0;
//            }
//        }
//
//        if (score < 0) score = 0;
//
//        emailRecord.setIsValid(isValid);
//        emailRecord.setIsSpam(isSpam);
//        emailRecord.setIsDisposable(isDisposable);
//        emailRecord.setScore(score);
//        emailRecord.setValidationDetails(validationDetails);
//        emailRecord.setEmailStatus(StatusDecide.calculateStatus(score));
//
//        emailRecordRepository.save(emailRecord);

        return null;
    }

    private EmailResponse map(EmailRecord record) {

//        EmailResponse response = new EmailResponse();
//
//        response.setEmail(record.getEmail());
//        response.setDomain(record.getDomain());
//        response.setIsValid(record.getIsValid());
//        response.setIsDisposable(record.getIsDisposable());
//        response.setIsSpam(record.getIsSpam());
//        response.setScore(record.getScore());
//        response.setCreatedAt(record.getCreatedAt());
//        response.setEmailStatus(record.getEmailStatus());

        // Map ValidationDetail to DTO
//        if (record.getValidationDetails() != null) {
//            List<ValidationDetailDto> details = new ArrayList<>();
//            for (ValidationDetail detail : record.getValidationDetails()) {
//                ValidationDetailDto dto = new ValidationDetailDto();
//                dto.setCheckName(detail.getCheckName());
//                dto.setIsPassed(detail.getIsPassed());
//                dto.setMessage(detail.getMessage());
//                dto.setImpactScore(detail.getImpactScore());
//                dto.setCheckedAt(detail.getCheckedAt());
//                details.add(dto);
//            }
//            response.setDetails(details);
//        }
        return null;
    }
}