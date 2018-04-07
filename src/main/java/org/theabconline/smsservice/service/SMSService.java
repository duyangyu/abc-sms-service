package org.theabconline.smsservice.service;

import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper;

    @Autowired
    public SMSService(ValidationService validationService,
                      ParserService parserService,
                      AliyunSMSAdapter aliyunSMSAdapter,
                      ObjectMapper objectMapper) {
        this.validationService = validationService;
        this.parserService = parserService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
        this.objectMapper = objectMapper;
    }

    public void send(String message, String timestamp, String nonce, String sha1) throws IOException, ClientException {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.info("Invalid message");
            throw new RuntimeException("Message invalid");
        }

        SmsVO smsVO = parserService.getSmsParams(message);
        LOGGER.debug("Params: {}", objectMapper.writeValueAsString(smsVO));
        aliyunSMSAdapter.sendMessage(smsVO);
    }
}
