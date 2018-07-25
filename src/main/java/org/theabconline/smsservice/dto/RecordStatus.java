package org.theabconline.smsservice.dto;

import com.google.common.collect.Maps;

import java.util.Map;

public class RecordStatus {

    private String appId;

    private String entryId;

    private String dataId;

    private Map<String, MessageStatus> bizIdStatusMap;

    public RecordStatus(String appId, String entryId, String dataId) {
        this(appId, entryId, dataId, Maps.<String, MessageStatus>newConcurrentMap());
    }

    public RecordStatus(String appId, String entryId, String dataId, Map<String, MessageStatus> bizIdStatusMap) {
        this.appId = appId;
        this.entryId = entryId;
        this.dataId = dataId;
        this.bizIdStatusMap = bizIdStatusMap;
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

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public Map<String, MessageStatus> getBizIdStatusMap() {
        return bizIdStatusMap;
    }

    public void setBizIdStatusMap(Map<String, MessageStatus> bizIdStatusMap) {
        this.bizIdStatusMap = bizIdStatusMap;
    }
}
