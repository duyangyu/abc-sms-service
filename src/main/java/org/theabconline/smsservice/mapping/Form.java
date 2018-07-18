package org.theabconline.smsservice.mapping;

import org.springframework.stereotype.Component;

@Component
public class Form {

    private String name;
    private String appId;
    private String entryId;
    private String metadataWidget;
    private String messageSentWidget;
    private String errorMessageWidget;

    /* Custom code */
    public String getFormId() {
        return this.appId + this.entryId;
    }

    /* Generated code */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getMetadataWidget() {
        return metadataWidget;
    }

    public void setMetadataWidget(String metadataWidget) {
        this.metadataWidget = metadataWidget;
    }

    public String getMessageSentWidget() {
        return messageSentWidget;
    }

    public void setMessageSentWidget(String messageSentWidget) {
        this.messageSentWidget = messageSentWidget;
    }

    public String getErrorMessageWidget() {
        return errorMessageWidget;
    }

    public void setErrorMessageWidget(String errorMessageWidget) {
        this.errorMessageWidget = errorMessageWidget;
    }
}
