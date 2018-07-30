package org.theabconline.smsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ErrorHandlingService {

    private final EmailService emailService;

    @Autowired
    public ErrorHandlingService(EmailService emailService) {
        this.emailService = emailService;
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
}
