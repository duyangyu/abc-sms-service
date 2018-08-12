package org.theabconline.smsservice.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JdyRecordFields {
    public static final String KEY = "value";

    @Value("${jdyun.report.templateId.widget}")
    private String templateIdWidget;
    @Value("${jdyun.report.sentOn.widget}")
    private String sentOnWidget;
    @Value("${jdyun.report.phoneNumbers.widget}")
    private String phoneNumbersWidget;
    @Value("${jdyun.report.content.widget}")
    private String contentWidget;
    @Value("${jdyun.report.numbersAmount.widget}")
    private String numbersAmountWidget;
    @Value("${jdyun.report.status.widget}")
    private String statusWidget;
    @Value("${jdyun.report.successAmount.widget}")
    private String successAmountWidget;
    @Value("${jdyun.report.failAmount.widget}")
    private String failAmountWidget;
    @Value("${jdyun.report.failPhoneNumbers.widget}")
    private String failPhoneNumbersWidget;
    @Value("${jdyun.report.errorMessage.widget}")
    private String errorMessageWidget;

    public String getTemplateIdWidget() {
        return templateIdWidget;
    }

    public String getSentOnWidget() {
        return sentOnWidget;
    }

    public String getPhoneNumbersWidget() {
        return phoneNumbersWidget;
    }

    public String getContentWidget() {
        return contentWidget;
    }

    public String getNumbersAmountWidget() {
        return numbersAmountWidget;
    }

    public String getStatusWidget() {
        return statusWidget;
    }

    public String getSuccessAmountWidget() {
        return successAmountWidget;
    }

    public String getFailAmountWidget() {
        return failAmountWidget;
    }

    public String getFailPhoneNumbersWidget() {
        return failPhoneNumbersWidget;
    }

    public String getErrorMessageWidget() {
        return errorMessageWidget;
    }
}
