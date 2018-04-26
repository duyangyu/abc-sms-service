package org.theabconline.smsservice.exception;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;

public class SendSmsException extends RuntimeException {

    private SendSmsResponse sendSmsResponse;

    public SendSmsException(SendSmsResponse sendSmsResponse) {
        super();
        this.sendSmsResponse = sendSmsResponse;
    }

    public SendSmsResponse getSendSmsResponse() {
        return sendSmsResponse;
    }
}
