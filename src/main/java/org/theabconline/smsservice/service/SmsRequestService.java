package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.SmsRequestDTO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.SmsRequestRepository;

import java.io.IOException;

@Service
public class SmsRequestService {

    private final SmsRequestRepository smsRequestRepository;

    private final ParsingService parsingService;

    private final SmsMessageService smsMessageService;

    private final ErrorHandlingService errorHandlingService;

    private final AliyunSMSAdapter aliyunSMSAdapter;

    @Autowired
    public SmsRequestService(SmsRequestRepository smsRequestRepository,
                             ParsingService parsingService,
                             SmsMessageService smsMessageService,
                             ErrorHandlingService errorHandlingService,
                             AliyunSMSAdapter aliyunSMSAdapter) {
        this.smsRequestRepository = smsRequestRepository;
        this.parsingService = parsingService;
        this.smsMessageService = smsMessageService;
        this.errorHandlingService = errorHandlingService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
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
            errorHandlingService.handleParsingFailed(parsingException, rawMessage);
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
                errorHandlingService.handleSendingFailed(phoneNumbers, templateCode, payload, errorMessage);
            }
        } catch (ClientException e) {
            smsRequestBO.setErrorMessage(e.getMessage());
        }

        smsRequestRepository.save(smsRequestBO);
        smsMessageService.saveSmsMessages(smsRequestBO);
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

    private boolean isMessageSent(SendSmsResponse sendSmsResponse) {
        return sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK");
    }

}
