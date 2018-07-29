package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.theabconline.smsservice.dto.SmsRequestDTO;
import org.theabconline.smsservice.entity.RawMessageBO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.RawMessageRepository;
import org.theabconline.smsservice.repository.RecordRepository;
import org.theabconline.smsservice.repository.SmsMessageRepository;
import org.theabconline.smsservice.repository.SmsRequestRepository;

import java.io.IOException;
import java.util.List;

@Service
public class SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsService.class);

    private final RawMessageRepository rawMessageRepository;

    private final RecordRepository recordRepository;

    private final SmsRequestRepository smsRequestRepository;

    private final SmsMessageRepository smsMessageRepository;

    private final ValidationService validationService;

    private final ParsingService parsingService;

    private final AliyunSMSAdapter aliyunSMSAdapter;

    private final EmailService emailService;

    @Value("${checkBlocking.threshold:10}")
    private Integer blockingThreshold;

    @Autowired
    public SmsService(RawMessageRepository rawMessageRepository,
                      RecordRepository recordRepository,
                      SmsRequestRepository smsRequestRepository,
                      SmsMessageRepository smsMessageRepository,
                      ValidationService validationService,
                      ParsingService parsingService,
                      AliyunSMSAdapter aliyunSMSAdapter,
                      EmailService emailService) {
        this.rawMessageRepository = rawMessageRepository;
        this.recordRepository = recordRepository;
        this.smsRequestRepository = smsRequestRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.validationService = validationService;
        this.parsingService = parsingService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
        this.emailService = emailService;
    }

    @Transactional
    public void saveMessage(String message, String timestamp, String nonce, String sha1) {
        validateMessage(message, timestamp, nonce, sha1);
        RawMessageBO rawMessageBO = createRawMessageBO(message);
        rawMessageRepository.save(rawMessageBO);
    }

    @Transactional
    public void processRawMessages() {
        List<RawMessageBO> unprocessedMessages = rawMessageRepository.getRawMessageBOSByIsProcessedFalse();
        for (RawMessageBO unprocessedMessage : unprocessedMessages) {
            processRawMessage(unprocessedMessage);
            unprocessedMessage.setProcessed(true);
        }
        rawMessageRepository.save(unprocessedMessages);
    }

    void processRawMessage(RawMessageBO rawMessageBO) {
        String appId = null;
        String entryId = null;
        String dataId = null;
        FormMetadata formMetadata = null;
        Exception parsingException = null;
        try {
            appId = parsingService.getAppId(rawMessageBO.getMessage());
            entryId = parsingService.getEntryId(rawMessageBO.getMessage());
            dataId = parsingService.getDataId(rawMessageBO.getMessage());
            formMetadata = parsingService.getFormMetadata(rawMessageBO.getMessage());
        } catch (Exception e) {
            parsingException = e;
        }

        RecordBO recordBO = createRecordBO(rawMessageBO.getId(), appId, entryId, dataId, parsingException);
        recordRepository.save(recordBO);

        if (parsingException != null) {
            handleParsingFailed(parsingException, rawMessageBO.getMessage());
            return;
        }

        for (SmsTemplate smsTemplate : formMetadata.getSmsTemplates()) {
            processSmsRequest(rawMessageBO.getMessage(), recordBO, smsTemplate);
        }
    }

    void processSmsRequest(String rawMessage, RecordBO recordBO, SmsTemplate smsTemplate) {
        String phoneNumbers = null;
        String templateCode = null;
        String payload = null;
        IOException parsingException = null;
        try {
            phoneNumbers = parsingService.getPhoneNumbers(rawMessage, smsTemplate.getPhoneNumbersWidget());
            if (Strings.isNullOrEmpty(phoneNumbers) || Strings.isNullOrEmpty(phoneNumbers.trim())) {
                return;
            }
            templateCode = smsTemplate.getSmsTemplateCode();
            payload = parsingService.getPayload(rawMessage, smsTemplate.getFieldMappings());
        } catch (IOException e) {
            parsingException = e;
        }

        SmsRequestBO smsRequestBO = createSmsRequestBO(recordBO.getId(), phoneNumbers, templateCode, payload, parsingException);

        if (parsingException != null) {
            smsRequestRepository.save(smsRequestBO);
            handleParsingFailed(parsingException, rawMessage);
            return;
        }

        SmsRequestDTO smsRequestDTO = createSmsRequestDTO(phoneNumbers, templateCode, payload);
        try {
            SendSmsResponse sendSmsResponse = aliyunSMSAdapter.sendMessage(smsRequestDTO);
            if (isMessageSent(sendSmsResponse)) {
                smsRequestBO.setSent(true);
                smsRequestBO.setBizId(sendSmsResponse.getBizId());
            } else {
                String errorMessage = sendSmsResponse.getMessage();
                smsRequestBO.setErrorMessage(errorMessage);
                handleSendingFailed(phoneNumbers, templateCode, payload, errorMessage);
            }
        } catch (ClientException e) {
            smsRequestBO.setErrorMessage(e.getMessage());
        }

        smsRequestRepository.save(smsRequestBO);
        saveSmsMessages(smsRequestBO);
    }

    void saveSmsMessages(SmsRequestBO smsRequestBO) {
        List<String> phoneNumbersList = getTrimmedPhoneNumberList(smsRequestBO.getPhoneNumbers());

        List<SmsMessageBO> smsMessageBOList = Lists.newArrayList();
        for (String phoneNumber : phoneNumbersList) {
            SmsMessageBO smsMessageBO = createSmsMessageBO(smsRequestBO, phoneNumber);
            smsMessageBOList.add(smsMessageBO);
        }
        smsMessageRepository.save(smsMessageBOList);
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

    private void validateMessage(String message, String timestamp, String nonce, String sha1) {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.error("Validation failed, timestamp: {}\n nonce: {}\n sha1: {}\n payload: {}", timestamp, nonce, sha1, message);
            throw new RuntimeException("Invalid Message");
        }
    }

    private RawMessageBO createRawMessageBO(String message) {
        RawMessageBO bo = new RawMessageBO();
        bo.setMessage(message);
        bo.setProcessed(false);

        return bo;
    }

    private RecordBO createRecordBO(Long rawMessageId, String appId, String entryId, String dataId, Exception e) {
        RecordBO recordBO = new RecordBO();
        recordBO.setAppId(appId);
        recordBO.setEntryId(entryId);
        recordBO.setDataId(dataId);
        recordBO.setErrorMessage(e == null ? null : e.getMessage());
        recordBO.setRawMessageId(rawMessageId);
        return recordBO;
    }

    private SmsRequestBO createSmsRequestBO(Long recordId, String phoneNumbers, String templateCode, String payload, IOException parsingException) {
        SmsRequestBO smsRequestBO = new SmsRequestBO();
        smsRequestBO.setTemplateCode(templateCode);
        smsRequestBO.setPhoneNumbers(phoneNumbers);
        smsRequestBO.setPayload(payload);
        smsRequestBO.setSent(false);
        smsRequestBO.setErrorMessage(parsingException == null ? null : parsingException.getMessage());
        smsRequestBO.setRecordId(recordId);
        return smsRequestBO;
    }

    private SmsRequestDTO createSmsRequestDTO(String phoneNumbers, String templateCode, String payload) {
        SmsRequestDTO smsRequestDTO = new SmsRequestDTO();
        smsRequestDTO.setTemplateCode(templateCode);
        smsRequestDTO.setPhoneNumber(phoneNumbers);
        smsRequestDTO.setParams(payload);
        return smsRequestDTO;
    }

    private SmsMessageBO createSmsMessageBO(SmsRequestBO smsRequestBO, String phoneNumber) {
        SmsMessageBO smsMessageBO = new SmsMessageBO();
        smsMessageBO.setPhoneNumber(phoneNumber);
        smsMessageBO.setContent(smsRequestBO.getPayload());
        smsMessageBO.setBizId(smsRequestBO.getBizId());
        smsMessageBO.setSent(false);
        smsMessageBO.setSmsRequestId(smsRequestBO.getId());
        return smsMessageBO;
    }

    private boolean isMessageSent(SendSmsResponse sendSmsResponse) {
        return sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK");
    }

    private List<String> getTrimmedPhoneNumberList(String phoneNumbers) {
        List<String> phoneNumbersList = Lists.newArrayList();
        List<String> rawPhoneNumbers = Lists.newArrayList(phoneNumbers.split(","));
        for (String rawPhoneNumber : rawPhoneNumbers) {
            phoneNumbersList.add(rawPhoneNumber.trim());
        }
        return phoneNumbersList;
    }
}
