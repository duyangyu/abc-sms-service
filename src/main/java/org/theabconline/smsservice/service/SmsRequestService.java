package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.JdyRecordDTO;
import org.theabconline.smsservice.dto.SmsRequestDTO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.SmsMessageRepository;
import org.theabconline.smsservice.repository.SmsRequestRepository;
import org.theabconline.smsservice.utils.JdyRecordFields;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SmsRequestService {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final SmsRequestRepository smsRequestRepository;

    private final SmsMessageRepository smsMessageRepository;

    private final ParsingService parsingService;

    private final SmsMessageService smsMessageService;

    private final JdyService jdyService;

    private final JdyRecordFields jdyRecordFields;

    private final ErrorHandlingService errorHandlingService;

    private final AliyunSMSAdapter aliyunSMSAdapter;

    private final ObjectMapper objectMapper;

    @Autowired
    public SmsRequestService(SmsRequestRepository smsRequestRepository,
                             SmsMessageRepository smsMessageRepository,
                             ParsingService parsingService,
                             SmsMessageService smsMessageService,
                             JdyService jdyService,
                             JdyRecordFields jdyRecordFields,
                             ErrorHandlingService errorHandlingService,
                             AliyunSMSAdapter aliyunSMSAdapter,
                             ObjectMapper objectMapper) {
        this.smsRequestRepository = smsRequestRepository;
        this.smsMessageRepository = smsMessageRepository;
        this.parsingService = parsingService;
        this.smsMessageService = smsMessageService;
        this.jdyService = jdyService;
        this.jdyRecordFields = jdyRecordFields;
        this.errorHandlingService = errorHandlingService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
        this.objectMapper = objectMapper;
    }

    void processSmsRequest(String rawMessage, RecordBO recordBO, SmsTemplate smsTemplate) {
        String phoneNumbers = null;
        String templateCode = null;
        String payload = null;
        String content = null;
        IOException parsingException = null;
        try {
            phoneNumbers = parsingService.getPhoneNumbers(rawMessage, smsTemplate.getPhoneNumbersWidget());
            if (Strings.isNullOrEmpty(phoneNumbers) || Strings.isNullOrEmpty(phoneNumbers.trim())) {
                return;
            }
            templateCode = smsTemplate.getSmsTemplateCode();
            payload = parsingService.getPayload(rawMessage, smsTemplate.getFieldMappings());
            content = parsingService.getFieldValue(rawMessage, smsTemplate.getContentWidget());
        } catch (IOException e) {
            parsingException = e;
        }

        SmsRequestBO smsRequestBO = createSmsRequestBO(recordBO.getId(), phoneNumbers, templateCode, payload, parsingException, content);

        if (parsingException != null) {
            smsRequestBO.setUpdateCount(Integer.MAX_VALUE);
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

        String dataId = createNewRecord(smsRequestBO);
        smsRequestBO.setDataId(dataId);
        smsRequestBO.setUpdateCount(dataId == null ? Integer.MAX_VALUE : 0);
        smsRequestBO.setUpdatedOn(new Date());
    }

    void updateRequestStatus(Integer maxCount, Long lastUpdatedInMillis) {
        List<SmsRequestBO> smsRequests = smsRequestRepository.findAllByUpdateCountLessThanEqualAndUpdatedOnBefore(maxCount, new Date(System.currentTimeMillis() - lastUpdatedInMillis));
        for (SmsRequestBO smsRequestBO : smsRequests) {
            updateRecord(smsRequestBO);
            smsRequestBO.setUpdateCount(smsRequestBO.getUpdateCount() + 1);
            smsRequestBO.setUpdatedOn(new Date());
            smsRequestRepository.save(smsRequestBO);
        }
    }

    private String createNewRecord(SmsRequestBO smsRequestBO) {
        String templateId = smsRequestBO.getTemplateCode();
        Date sentOn = smsRequestBO.getCreatedOn();
        String phoneNumbers = smsRequestBO.getPhoneNumbers();
        String content = smsRequestBO.getContent();
        Integer totalAmount = Lists.newArrayList(smsRequestBO.getPhoneNumbers().split(",")).size();

        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO();
        Map<String, Map<String, Object>> data = Maps.newHashMap();
        data.put(jdyRecordFields.getTemplateIdWidget(), getValueMap(templateId));
        data.put(jdyRecordFields.getSentOnWidget(), getValueMap(DATE_FORMAT.format(sentOn)));
        data.put(jdyRecordFields.getPhoneNumbersWidget(), getValueMap(phoneNumbers));
        data.put(jdyRecordFields.getContentWidget(), getValueMap(content));
        data.put(jdyRecordFields.getNumbersAmountWidget(), getValueMap(totalAmount));
        data.put(jdyRecordFields.getStatusWidget(), getValueMap(smsRequestBO.getSent() ? "Sent" : "NOT Sent"));
        data.put(jdyRecordFields.getErrorMessageWidget(), getValueMap(smsRequestBO.getErrorMessage()));
        jdyRecordDTO.setData(data);

        String response = jdyService.createReportRecord(jdyRecordDTO);
        String dataId = null;

        try {
            dataId = parsingService.getFieldValue(response, "_id");
        } catch (IOException e) {
            errorHandlingService.handleJdyFailure(getErrorMessage(response, data));
        }

        return dataId;
    }

    private String updateRecord(SmsRequestBO smsRequestBO) {
        String dataId = smsRequestBO.getDataId();
        Integer successAmount = smsMessageRepository.countAllBySmsRequestIdAndIsSent(smsRequestBO.getId(), Boolean.TRUE);
        List<String> failedPhoneNumbers = getFailedPhoneNumbers(smsRequestBO);
        String errorMessage = getErrorMessage(smsRequestBO);

        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO();
        jdyRecordDTO.setData_id(dataId);
        Map<String, Map<String, Object>> data = Maps.newHashMap();
        data.put(jdyRecordFields.getSuccessAmountWidget(), getValueMap(successAmount));
        data.put(jdyRecordFields.getFailAmountWidget(), getValueMap(failedPhoneNumbers.size()));
        data.put(jdyRecordFields.getFailPhoneNumbersWidget(), getValueMap(Joiner.on(",").join(failedPhoneNumbers)));
        data.put(jdyRecordFields.getErrorMessageWidget(), getValueMap(errorMessage));
        jdyRecordDTO.setData(data);

        String response = jdyService.updateRecordMessage(jdyRecordDTO);
        if (response == null) {
            errorHandlingService.handleJdyFailure(getErrorMessage("Possible cause: report data deleted, dataId: " + dataId, data));
        }

        return response;
    }

    private List<String> getFailedPhoneNumbers(SmsRequestBO smsRequestBO) {
        List<String> failedPhoneNumbers = Lists.newArrayList();
        List<SmsMessageBO> failedMessages = smsMessageRepository.getAllBySmsRequestIdAndIsSent(smsRequestBO.getId(), Boolean.FALSE);
        for (SmsMessageBO smsMessageBO : failedMessages) {
            failedPhoneNumbers.add(smsMessageBO.getPhoneNumber());
        }

        return failedPhoneNumbers;
    }

    private String getErrorMessage(SmsRequestBO smsRequestBO) {
        StringBuilder sb = new StringBuilder();
        List<SmsMessageBO> failedMessages = smsMessageRepository.getAllBySmsRequestIdAndIsSent(smsRequestBO.getId(), Boolean.FALSE);
        if (smsRequestBO.getErrorMessage() != null) {
            sb.append(smsRequestBO.getErrorMessage()).append(",");
        }
        for (SmsMessageBO failedMessage : failedMessages) {
            sb.append(failedMessage.getPhoneNumber()).append(":").append(Objects.toString(failedMessage.getErrorMessage(), "")).append(",");
        }

        return sb.toString();
    }

    private String getErrorMessage(String response, Map<String, Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Response: ").append(response).append("\n");
        String dataString = null;
        try {
            dataString = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        sb.append("Payload: ").append(Objects.toString(dataString, ""));

        return sb.toString();
    }

    private Map<String, Object> getValueMap(Object value) {
        Map<String, Object> valueMap = Maps.newHashMap();
        valueMap.put(JdyRecordFields.KEY, value);

        return valueMap;
    }

    private SmsRequestBO createSmsRequestBO(Long recordId, String phoneNumbers, String templateCode, String payload,
                                            IOException parsingException, String content) {
        SmsRequestBO smsRequestBO = new SmsRequestBO();
        smsRequestBO.setTemplateCode(templateCode);
        smsRequestBO.setPhoneNumbers(phoneNumbers);
        smsRequestBO.setPayload(payload);
        smsRequestBO.setSent(false);
        smsRequestBO.setErrorMessage(parsingException == null ? null : parsingException.getMessage());
        smsRequestBO.setRecordId(recordId);
        smsRequestBO.setContent(content);
        smsRequestBO.setUpdateCount(0);
        smsRequestBO.setUpdatedOn(new Date());
        smsRequestBO.setCreatedOn(new Date());

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
