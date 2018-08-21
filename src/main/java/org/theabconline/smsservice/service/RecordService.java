package org.theabconline.smsservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.theabconline.smsservice.entity.RawMessageBO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.RawMessageRepository;
import org.theabconline.smsservice.repository.RecordRepository;

import java.util.List;

@Service
public class RecordService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordService.class);

    private final RawMessageRepository rawMessageRepository;

    private final RecordRepository recordRepository;

    private final ValidationService validationService;

    private final ParsingService parsingService;

    private final SmsRequestService smsRequestService;

    private final ErrorHandlingService errorHandlingService;

    @Value("${checkBlocking.threshold:10}")
    private Integer blockingThreshold;

    @Autowired
    public RecordService(RawMessageRepository rawMessageRepository,
                         RecordRepository recordRepository,
                         ValidationService validationService,
                         ParsingService parsingService,
                         SmsRequestService smsRequestService,
                         ErrorHandlingService errorHandlingService) {
        this.rawMessageRepository = rawMessageRepository;
        this.recordRepository = recordRepository;
        this.validationService = validationService;
        this.parsingService = parsingService;
        this.smsRequestService = smsRequestService;
        this.errorHandlingService = errorHandlingService;
    }

    @Transactional
    public void saveRawMessage(String message, String timestamp, String nonce, String sha1) {
//        validateMessage(message, timestamp, nonce, sha1);
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

    @Transactional
    public void checkBlocking() {
        Long numberMessagesNotProcessed = rawMessageRepository.countByIsProcessedFalse();
        if (numberMessagesNotProcessed > blockingThreshold) {
            errorHandlingService.handleBlocking(numberMessagesNotProcessed);
        }
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
            errorHandlingService.handleParsingFailed(parsingException, rawMessageBO.getMessage());
            return;
        }

        for (SmsTemplate smsTemplate : formMetadata.getSmsTemplates()) {
            smsRequestService.processSmsRequest(rawMessageBO.getMessage(), recordBO, smsTemplate);
        }
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

}
