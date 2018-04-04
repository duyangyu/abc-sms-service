package org.theabconline.smsservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SMSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    private final ValidationService validationService;

    private final ParserService parserService;

    private final AliyunSMSAdapter aliyunSMSAdapter;

    @Autowired
    public SMSService(ValidationService validationService, ParserService parserService, AliyunSMSAdapter aliyunSMSAdapter) {
        this.validationService = validationService;
        this.parserService = parserService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
    }

    public void send(String message, String timestamp, String nonce, String sha1) throws IOException {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.info("Invalid message");
            throw new RuntimeException("Message invalid");
        }

        String params = parserService.getParams(message);
        LOGGER.debug("Params: {}", params);
        aliyunSMSAdapter.sendMessage(params);
    }
}
