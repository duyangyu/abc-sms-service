package org.theabconline.smsservice.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.repository.SmsMessageRepository;

import java.util.List;

@Service
public class SmsMessageService {

    private final SmsMessageRepository smsMessageRepository;

    @Autowired
    public SmsMessageService(SmsMessageRepository smsMessageRepository) {
        this.smsMessageRepository = smsMessageRepository;
    }

    void saveSmsMessages(SmsRequestBO smsRequestBO) {
        List<String> phoneNumbersList = getTrimmedPhoneNumberList(smsRequestBO.getPhoneNumbers());

        List<SmsMessageBO> smsMessageBOList = Lists.newArrayList();
        for (String phoneNumber : phoneNumbersList) {
            SmsMessageBO smsMessageBO = createSmsMessageBO(smsRequestBO, phoneNumber);
            smsMessageBOList.add(smsMessageBO);
        }
        smsMessageRepository.save(smsMessageBOList);
    }

    private SmsMessageBO createSmsMessageBO(SmsRequestBO smsRequestBO, String phoneNumber) {
        SmsMessageBO smsMessageBO = new SmsMessageBO();
        smsMessageBO.setPhoneNumber(phoneNumber);
        smsMessageBO.setContent(smsRequestBO.getPayload());
        smsMessageBO.setBizId(smsRequestBO.getBizId());
        smsMessageBO.setSent(false);
        smsMessageBO.setSmsRequestId(smsRequestBO.getId());
        return smsMessageBO;
    }

    private List<String> getTrimmedPhoneNumberList(String phoneNumbers) {
        List<String> phoneNumbersList = Lists.newArrayList();
        List<String> rawPhoneNumbers = Lists.newArrayList(phoneNumbers.split(","));
        for (String rawPhoneNumber : rawPhoneNumbers) {
            phoneNumbersList.add(rawPhoneNumber.trim());
        }
        return phoneNumbersList;
    }

}
