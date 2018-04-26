package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.theabconline.smsservice.exception.SendSmsException;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SMSServiceTest {

    @InjectMocks
    private SMSService fixture;

    @Mock
    ValidationService validationService;

    @Mock
    ParserService parserService;

    @Mock
    AliyunSMSAdapter aliyunSMSAdapter;

    @Mock
    EmailService emailService;

    @Mock
    LogService logService;

    @Captor
    ArgumentCaptor<SmsVO> smsVOCaptor1;

    @Captor
    ArgumentCaptor<SmsVO> smsVOCaptor2;

    @Captor
    ArgumentCaptor<SmsVO> smsVOCaptor3;

    @Captor
    ArgumentCaptor<String> errorMessageCaptor1;

    @Captor
    ArgumentCaptor<String> errorMessageCaptor2;

    @Test
    public void testSendHappyPath() throws IOException {
        SmsVO smsVO = new SmsVO();
        List<SmsVO> smsVOList = Lists.newArrayList(smsVO);

        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(parserService.getSmsParams(Mockito.anyString())).thenReturn(smsVOList);

        fixture.send("message", "timestamp", "nonce", "sha1");

        Queue<SmsVO> queue = (Queue<SmsVO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        assertEquals(smsVOList.size(), queue.size());
        assertEquals(smsVO, queue.poll());
    }

    @Test(expected = RuntimeException.class)
    public void testSendInvalidMessage() throws IOException {
        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);

        fixture.send("message", "timestamp", "nonce", "sha1");

        fail("Exception expected");
    }

    @Test
    public void testProcessQueueHappyPath() throws ClientException {
        SmsVO smsVO = new SmsVO();
        Queue<SmsVO> queue = (Queue<SmsVO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsVO);
        Mockito.doNothing().when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        assertEquals(smsVO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
    }

    @Test
    public void testProcessQueueClientException() throws ClientException {
        SmsVO smsVO = new SmsVO();
        Queue<SmsVO> queue = (Queue<SmsVO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsVO);
        String errCode = "errCode";
        String errMsg = "errMsg";
        ClientException clientException = new ClientException(errCode, errMsg);
        Mockito.doThrow(clientException).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsVO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsVO, smsVOCaptor2.getValue());
        assertEquals(errMsg, errorMessageCaptor1.getValue());
        assertEquals(smsVO, smsVOCaptor3.getValue());
        assertEquals(errMsg, errorMessageCaptor2.getValue());
    }

    @Test
    public void testProcessQueueSendSmsException() throws ClientException {
        SmsVO smsVO = new SmsVO();
        Queue<SmsVO> queue = (Queue<SmsVO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsVO);
        String errMsg = "failure message";
        SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setMessage(errMsg);
        SendSmsException sendSmsException = new SendSmsException(sendSmsResponse);
        Mockito.doThrow(sendSmsException).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsVO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsVO, smsVOCaptor2.getValue());
        assertEquals(errMsg, errorMessageCaptor1.getValue());
        assertEquals(smsVO, smsVOCaptor3.getValue());
        assertEquals(errMsg, errorMessageCaptor2.getValue());
    }

    @Test
    public void testProcessGeneralException() throws ClientException {
        SmsVO smsVO = new SmsVO();
        Queue<SmsVO> queue = (Queue<SmsVO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsVO);
        String errMsg = "exception message";
        Exception exception = new RuntimeException(errMsg);
        Mockito.doThrow(exception).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsVO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsVO, smsVOCaptor2.getValue());
        assertTrue(errorMessageCaptor1.getValue().contains(errMsg));
        assertEquals(smsVO, smsVOCaptor3.getValue());
        assertTrue(errorMessageCaptor2.getValue().contains(errMsg));
    }
}
