package org.theabconline.smsservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.repository.SmsMessageRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class SmsMessageServiceTest {

    @InjectMocks
    SmsMessageService fixture;

    @Mock
    SmsMessageRepository smsMessageRepository;

    @Test
    public void testSaveSmsMessages() {
        Long id = 1L;
        String payload = "payload";
        String bizId = "biz id";
        String phoneNumber1 = "phone number1";
        String phoneNumber2 = "pohne number2";
        String phoneNumbers = String.format(" %s, %s ", phoneNumber1, phoneNumber2);
        SmsRequestBO smsRequestBO = new SmsRequestBO();
        smsRequestBO.setId(id);
        smsRequestBO.setPayload(payload);
        smsRequestBO.setBizId(bizId);
        smsRequestBO.setPhoneNumbers(phoneNumbers);
        ArgumentCaptor<List> smsMessageBOListArgumentCaprtor = ArgumentCaptor.forClass(List.class);

        fixture.saveSmsMessages(smsRequestBO);

        verify(smsMessageRepository, times(1)).save(smsMessageBOListArgumentCaprtor.capture());
        assertEquals(2, smsMessageBOListArgumentCaprtor.getValue().size());
        SmsMessageBO smsMessageBO1 = (SmsMessageBO) smsMessageBOListArgumentCaprtor.getValue().get(0);
        assertEquals(payload, smsMessageBO1.getContent());
        assertEquals(bizId, smsMessageBO1.getBizId());
        assertEquals(phoneNumber1, smsMessageBO1.getPhoneNumber());
        assertFalse(smsMessageBO1.getSent());
        assertNull(smsMessageBO1.getErrorMessage());
        assertEquals(id, smsMessageBO1.getSmsRequestId());

    }
}
