package org.theabconline.smsservice.dto;

public class SmsExceptionDTO extends SmsDTO {

    private String errorMessage;

    public SmsExceptionDTO(SmsDTO smsDTO, String errorMessage) {
        super(smsDTO.getPhoneNumber(), smsDTO.getTemplateCode(), smsDTO.getParams());
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
