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
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.SmsExceptionDTO;
import org.theabconline.smsservice.exception.SendSmsException;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SMSServiceTest {

    @InjectMocks
    private SMSService fixture;

    @Mock
    private ValidationService validationService;

    @Mock
    private ParserService parserService;

    @Mock
    private AliyunSMSAdapter aliyunSMSAdapter;

    @Mock
    private EmailService emailService;

    @Mock
    private LogService logService;

    @Captor
    private ArgumentCaptor<SmsDTO> aliyunAdapterCaptor;

    @Captor
    private ArgumentCaptor<SmsExceptionDTO> logServiceCaptor;

    @Captor
    private ArgumentCaptor<String> emailSubject;

    @Captor
    private ArgumentCaptor<String> emailText;

    @Test
    public void testSendHappyPath() throws IOException {
        SmsDTO smsDTO = new SmsDTO();
        List<SmsDTO> smsDTOList = Lists.newArrayList(smsDTO);

        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(parserService.getSmsParams(Mockito.anyString())).thenReturn(smsDTOList);

        fixture.send("message", "timestamp", "nonce", "sha1");

        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        assertEquals(smsDTOList.size(), queue.size());
        assertEquals(smsDTO, queue.poll());
    }

    @Test(expected = RuntimeException.class)
    public void testSendInvalidMessage() {
        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);

        fixture.send("message", "timestamp", "nonce", "sha1");

        fail("Exception expected");
    }

    @Test
    public void testProcessQueueHappyPath() throws ClientException {
        SmsDTO smsDTO = new SmsDTO();
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        Mockito.doNothing().when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());

        fixture.processQueue();

        assertEquals(smsDTO, aliyunAdapterCaptor.getValue());
        assertEquals(0, queue.size());
    }

    @Test
    public void testProcessQueueClientException() throws ClientException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errCode = "errCode";
        String errMsg = "errMsg";
        ClientException clientException = new ClientException(errCode, errMsg);
        Mockito.doThrow(clientException).when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(logServiceCaptor.capture());
        Mockito.verify(emailService, Mockito.times(1)).send(emailSubject.capture(), emailText.capture());

        assertEquals(smsDTO, aliyunAdapterCaptor.getValue());
        assertEquals(0, queue.size());
        assertEquals(phoneNumber, logServiceCaptor.getValue().getPhoneNumber());
        assertEquals(templateCode, logServiceCaptor.getValue().getTemplateCode());
        assertEquals(params, logServiceCaptor.getValue().getParams());
        assertEquals(errMsg, logServiceCaptor.getValue().getErrorMessage());
        assertTrue(emailSubject.getValue().contains(phoneNumber));
        assertTrue(emailText.getValue().contains(templateCode));
        assertTrue(emailText.getValue().contains(params));
        assertTrue(emailText.getValue().contains(errMsg));
    }

    @Test
    public void testProcessQueueSendSmsException() throws ClientException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errMsg = "failure message";
        SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setMessage(errMsg);
        SendSmsException sendSmsException = new SendSmsException(sendSmsResponse);
        Mockito.doThrow(sendSmsException).when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(logServiceCaptor.capture());
        Mockito.verify(emailService, Mockito.times(1)).send(emailSubject.capture(), emailText.capture());

        assertEquals(smsDTO, aliyunAdapterCaptor.getValue());
        assertEquals(0, queue.size());
        assertEquals(phoneNumber, logServiceCaptor.getValue().getPhoneNumber());
        assertEquals(templateCode, logServiceCaptor.getValue().getTemplateCode());
        assertEquals(params, logServiceCaptor.getValue().getParams());
        assertEquals(errMsg, logServiceCaptor.getValue().getErrorMessage());
        assertTrue(emailSubject.getValue().contains(phoneNumber));
        assertTrue(emailText.getValue().contains(templateCode));
        assertTrue(emailText.getValue().contains(params));
        assertTrue(emailText.getValue().contains(errMsg));
    }

    @Test
    public void testProcessGeneralException() throws ClientException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errMsg = "exception message";
        Exception exception = new RuntimeException(errMsg);
        Mockito.doThrow(exception).when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(logServiceCaptor.capture());
        Mockito.verify(emailService, Mockito.times(1)).send(emailSubject.capture(), emailText.capture());

        assertEquals(smsDTO, aliyunAdapterCaptor.getValue());
        assertEquals(0, queue.size());
        assertEquals(phoneNumber, logServiceCaptor.getValue().getPhoneNumber());
        assertEquals(templateCode, logServiceCaptor.getValue().getTemplateCode());
        assertEquals(params, logServiceCaptor.getValue().getParams());
        assertTrue(logServiceCaptor.getValue().getErrorMessage().contains(errMsg));
        assertTrue(emailSubject.getValue().contains(phoneNumber));
        assertTrue(emailText.getValue().contains(templateCode));
        assertTrue(emailText.getValue().contains(params));
        assertTrue(emailText.getValue().contains(errMsg));
    }

    @Test
    public void testCheckBlocking() {
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        Integer threshold = 2;
        ReflectionTestUtils.setField(fixture, "blockingThreshold", threshold);
        SmsDTO smsDTO = new SmsDTO();
        queue.add(smsDTO);
        queue.add(smsDTO);
        queue.add(smsDTO);

        fixture.checkBlocking();

        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyString(), Mockito.anyString());
    }
}
