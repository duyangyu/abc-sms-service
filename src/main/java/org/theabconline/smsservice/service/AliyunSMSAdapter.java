package org.theabconline.smsservice.service;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AliyunSMSAdapter {

    private static final String PRODUCT = "Dysmsapi";
    private static final String DOMAIN = "dysmsapi.aliyuncs.com";

    @Value("${aliyun.accessKey}")
    private String accessKey;

    @Value("${aliyun.secret}")
    private String secret;

    @Value("${aliyun.sms.signature}")
    private String signature;

    public void sendMessage(SmsVO smsVO) throws ClientException {
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKey, secret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
        IAcsClient acsClient = new DefaultAcsClient(profile);

        SendSmsRequest request = new SendSmsRequest();
        request.setMethod(MethodType.POST);
        request.setPhoneNumbers(smsVO.getPhoneNumber());
        request.setSignName(signature);
        request.setTemplateCode(smsVO.getTemplateCode());
        request.setTemplateParam(smsVO.getParams());

        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        if (sendSmsResponse.getCode() == null || !sendSmsResponse.getCode().equals("OK")) {
            throw new RuntimeException("Fail to send SMS message");
        }
    }
}
