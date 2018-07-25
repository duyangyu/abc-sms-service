package org.theabconline.smsservice.dto;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class MessageStatus {

    private String bizId;

    private String templateId;

    private String errorMessage;

    private Map<String, Boolean> messageSent;

    public MessageStatus(String bizId, String templateId, String errorMessage, Map<String, Boolean> messageSent) {
        this.bizId = bizId;
        this.templateId = templateId;
        this.errorMessage = errorMessage;
        this.messageSent = messageSent;
    }

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Boolean> getMessageSent() {
        return messageSent;
    }

    public void setMessageSent(Map<String, Boolean> messageSent) {
        this.messageSent = messageSent;
    }
}
