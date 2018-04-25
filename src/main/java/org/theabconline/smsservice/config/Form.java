package org.theabconline.smsservice.config;

import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class Form {

    @Value("${jdy.formPhoneNumberDefaultPath: /data}")
    private String defaultPath;

    private String appId;
    private String entryId;
    private String name;
    private List<String> templateCodes;
    private String phoneNumberPath;
    private List<String> phoneNumberFieldNames;

    /* Custom code */
    public String getFormId() {
        return this.appId + this.entryId;
    }

    public String getPhoneNumberPath() {
        return phoneNumberPath == null ? defaultPath : phoneNumberPath;
    }

    /* Generated code */
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTemplateCodes() {
        return templateCodes;
    }

    public void setTemplateCodes(List<String> templateCodes) {
        this.templateCodes = templateCodes;
    }

    public void setPhoneNumberPath(String phoneNumberPath) {
        this.phoneNumberPath = phoneNumberPath;
    }

    public List<String> getPhoneNumberFieldNames() {
        return phoneNumberFieldNames;
    }

    public void setPhoneNumberFieldNames(List<String> phoneNumberFieldNames) {
        this.phoneNumberFieldNames = phoneNumberFieldNames;
    }

}