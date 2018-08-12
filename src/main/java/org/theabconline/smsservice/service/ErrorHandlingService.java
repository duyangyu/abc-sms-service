package org.theabconline.smsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.entity.RecordBO;

@Service
public class ErrorHandlingService {

    private final EmailService emailService;

    private final ObjectMapper objectMapper;

    @Autowired
    public ErrorHandlingService(EmailService emailService,
                                ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    void handleParsingFailed(Exception parsingException, String rawMessage) {
        String subject = "SMS-Service - Parsing failed";
        String content = String.format("%s\n%s", parsingException.getMessage(), rawMessage);
        emailService.send(subject, content);
    }

    void handleSendingFailed(String phoneNumbers, String templateCode, String payload, String errorMessage) {
        String title = "SMS-Service - Sending SMS message failed";
        String content = String.format("Phone numbers: %s\nTemplate code: %s\nPayload: %s\nError message: %s", phoneNumbers, templateCode, payload, errorMessage);
        emailService.send(title, content);
    }

    void handleBlocking(Long numberMessagesNotProcessed) {
        String title = "SMS-Service - Blocking detected";
        String content = String.format("Number of unprocessed messaged: %d", numberMessagesNotProcessed);
        emailService.send(title, content);
    }

    public void handleJdyFailure(String errorMessage) {
        String title = "SMS-Service - Updating JDY record failed";
        emailService.send(title, errorMessage);
    }
}
