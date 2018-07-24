package org.theabconline.smsservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import java.util.Map;

public class JDYRecordDTO {

    @JsonIgnore
    String appId;

    @JsonIgnore
    String entryId;

    private String data_id;
    private Map<String, Map<String, String>> data;

    public JDYRecordDTO() {}

    public JDYRecordDTO(String appId,
                        String entryId,
                        String data_id,
                        String messageSentWidget,
                        boolean isMessageSent,
                        String errorMessageWidget,
                        String errorMessage
    ) {
        this.appId = appId;
        this.entryId = entryId;
        this.data_id = data_id;
        this.data = Maps.newHashMap();
        Map<String, String> messageSentNode = Maps.newHashMap();
        messageSentNode.put("value", String.valueOf(isMessageSent));
        Map<String, String> errorMessageNode = Maps.newHashMap();
        errorMessageNode.put("value", errorMessage);
        this.data.put(messageSentWidget, messageSentNode);
        this.data.put(errorMessageWidget, errorMessageNode);
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getData_id() {
        return data_id;
    }

    public void setData_id(String data_id) {
        this.data_id = data_id;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }

    public void setData(Map<String, Map<String, String>> data) {
        this.data = data;
    }
}
