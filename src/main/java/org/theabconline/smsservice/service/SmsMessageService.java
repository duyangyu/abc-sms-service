package org.theabconline.smsservice.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.repository.SmsMessageRepository;

import java.util.List;
import java.util.Objects;

@Service
public class SmsMessageService {

    private final SmsMessageRepository smsMessageRepository;

    @Autowired
    public SmsMessageService(SmsMessageRepository smsMessageRepository) {
        this.smsMessageRepository = smsMessageRepository;
    }

    public void saveSmsMessages(SmsRequestBO smsRequestBO) {
        List<String> phoneNumbersList = getTrimmedPhoneNumberList(smsRequestBO.getPhoneNumbers());

        List<SmsMessageBO> smsMessageBOList = Lists.newArrayList();
        for (String phoneNumber : phoneNumbersList) {
            SmsMessageBO smsMessageBO = createSmsMessageBO(smsRequestBO, phoneNumber);
            smsMessageBOList.add(smsMessageBO);
        }
        smsMessageRepository.save(smsMessageBOList);
    }

    public String getPhoneNumbersSent(RecordBO recordBO) {
        StringBuilder sb = new StringBuilder();
        List<SmsMessageBO> smsMessageBOList = smsMessageRepository.getAllByRecordIdAndIsSent(recordBO.getId(), true);

        for(SmsMessageBO smsMessageBO : smsMessageBOList) {
            sb.append(smsMessageBO.getPhoneNumber()).append(",");
        }

        return toString();
    }

    public String getPhoneNumbersNotSent(RecordBO recordBO) {
        StringBuilder sb = new StringBuilder();
        List<SmsMessageBO> smsMessageBOList = smsMessageRepository.getAllByRecordIdAndIsSent(recordBO.getId(), false);

        for(SmsMessageBO smsMessageBO : smsMessageBOList) {
            sb.append(smsMessageBO.getPhoneNumber())
                    .append(":")
                    .append(Objects.toString(smsMessageBO.getErrorMessage(), ""))
                    .append(",");
        }

        return sb.toString();
    }

    private SmsMessageBO createSmsMessageBO(SmsRequestBO smsRequestBO, String phoneNumber) {
        SmsMessageBO smsMessageBO = new SmsMessageBO();
        smsMessageBO.setPhoneNumber(phoneNumber);
        smsMessageBO.setContent(smsRequestBO.getPayload());
        smsMessageBO.setBizId(smsRequestBO.getBizId());
        smsMessageBO.setSent(false);
        smsMessageBO.setSmsRequestId(smsRequestBO.getId());
        smsMessageBO.setRecordId(smsRequestBO.getRecordId());
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
