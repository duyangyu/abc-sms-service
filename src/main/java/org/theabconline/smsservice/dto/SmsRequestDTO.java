package org.theabconline.smsservice.dto;

public class SmsRequestDTO {

    private String phoneNumber;
    private String templateCode;
    private String params;

    public SmsRequestDTO() {
    }

    public SmsRequestDTO(String phoneNumber, String templateCode, String params) {
        this.phoneNumber = phoneNumber;
        this.templateCode = templateCode;
        this.params = params;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }
}
