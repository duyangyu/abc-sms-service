package org.theabconline.smsservice.mapping;

import java.util.List;

public class SmsTemplate {

    private String smsTemplateCode;
    private String phoneNumbersWidget;
    private String contentWidget;
    private List<FieldMapping> fieldMappings;

    public String getSmsTemplateCode() {
        return smsTemplateCode;
    }

    public void setSmsTemplateCode(String smsTemplateCode) {
        this.smsTemplateCode = smsTemplateCode;
    }

    public String getPhoneNumbersWidget() {
        return phoneNumbersWidget;
    }

    public void setPhoneNumbersWidget(String phoneNumbersWidget) {
        this.phoneNumbersWidget = phoneNumbersWidget;
    }

    public String getContentWidget() {
        return contentWidget;
    }

    public SmsTemplate setContentWidget(String contentWidget) {
        this.contentWidget = contentWidget;
        return this;
    }

    public List<FieldMapping> getFieldMappings() {
        return fieldMappings;
    }

    public void setFieldMappings(List<FieldMapping> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }
}
