package org.theabconline.smsservice.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.MessageStatus;
import org.theabconline.smsservice.dto.RecordStatus;

import java.util.List;
import java.util.Map;

@Service
public class SMSTrackingService {

    private Map<String, RecordStatus> formIdRecordStatusMap;

    private JDYRecordService jdyRecordService;

    public SMSTrackingService(JDYRecordService jdyRecordService) {
        this.formIdRecordStatusMap = Maps.newConcurrentMap();
        this.jdyRecordService = jdyRecordService;
    }

    public void addSuccess(String appId, String entryId, String dataId, String templateCode, String bizId, String[] phoneNumbers) {
        add(appId, entryId, dataId, templateCode, bizId, phoneNumbers, "");
    }

    public void addFailure(String appId, String entryId, String dataId, String templateCode, String bizId, String[] phoneNumbers, String errorText) {
        add(appId, entryId, dataId, templateCode, bizId, phoneNumbers, errorText);
    }

    public void markAsSent(String bizId, String phoneNumber) {
        for (RecordStatus recordStatus : formIdRecordStatusMap.values()) {
            if (!recordStatus.getBizIdStatusMap().keySet().contains(bizId)) {
                continue;
            }
            MessageStatus messageStatus = recordStatus.getBizIdStatusMap().get(bizId);
            if (messageStatus.getMessageSent().keySet().contains(phoneNumber)) {
                messageStatus.getMessageSent().put(phoneNumber, Boolean.TRUE);
            }
        }
    }

    void add(String appId, String entryId, String dataId, String templateCode, String bizId, String[] phoneNumbers, String errorText) {
        String formId = getFormId(appId, entryId);
        RecordStatus recordStatus = this.formIdRecordStatusMap.get(formId);
        if (recordStatus == null) {
            recordStatus = new RecordStatus(appId, entryId, dataId);
            formIdRecordStatusMap.put(formId, recordStatus);
        }
        Map<String, Boolean> phoneNumberIsSentMap = getPhoneNumberList(phoneNumbers);
        MessageStatus messageStatus = new MessageStatus(bizId, templateCode, errorText, phoneNumberIsSentMap);
        recordStatus.getBizIdStatusMap().put(bizId, messageStatus);
    }

    private String getFormId(String appId, String entryId) {
        return appId + entryId;
    }

    private Map<String, Boolean> getPhoneNumberList(String[] phoneNumbers) {
        Map<String, Boolean> result = Maps.newConcurrentMap();
        List<String> rawPhoneNumbers = Lists.newArrayList(phoneNumbers);
        for(String rawPhoneNumber : rawPhoneNumbers) {
            result.put(rawPhoneNumber.trim(), Boolean.FALSE);
        }

        return result;
    }
}
