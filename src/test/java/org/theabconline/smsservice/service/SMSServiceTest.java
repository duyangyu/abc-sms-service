package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.theabconline.smsservice.dto.JDYRecordDTO;
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.SmsExceptionDTO;
import org.theabconline.smsservice.exception.SendSmsException;

import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
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

    @Mock
    private JDYRecordService jdyRecordService;

    @Captor
    private ArgumentCaptor<SmsDTO> aliyunAdapterCaptor;

    @Captor
    private ArgumentCaptor<SmsExceptionDTO> logServiceCaptor;

    @Captor
    private ArgumentCaptor<String> emailSubject;

    @Captor
    private ArgumentCaptor<String> emailText;

    @Captor
    private ArgumentCaptor<JDYRecordDTO> jdyRecordDTOCaptor;

    @Captor
    private ArgumentCaptor<String> errorMessageCaptor;

    @Test
    public void testSendHappyPath() throws IOException {
        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(parserService.getSmsDTOList(anyString())).thenReturn(Lists.newArrayList(new SmsDTO()));

        String message = "message";
        fixture.send(message, "timestamp", "nonce", "sha1");

        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        assertEquals(1, queue.size());
        assertEquals(message, queue.poll());
    }


    @Test(expected = RuntimeException.class)
    public void testSendInvalidMessage() {
        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        fixture.send("message", "timestamp", "nonce", "sha1");

        fail("Exception expected");
    }

    @Test
    public void testProcessQueueHappyPath() throws ClientException, IOException {
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setPhoneNumber("phone number");
        List<SmsDTO> smsDTOList = Lists.newArrayList(smsDTO);
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add("message");
        when(parserService.getSmsDTOList(anyString())).thenReturn(smsDTOList);
        doNothing().when(aliyunSMSAdapter).sendMessage(any(SmsDTO.class));

        fixture.processQueue();

        verify(aliyunSMSAdapter, times(1)).sendMessage(aliyunAdapterCaptor.capture());
        assertEquals(smsDTO, aliyunAdapterCaptor.getValue());
        assertEquals(0, queue.size());
    }

    @Test
    public void testProcessQueueWithNoPhoneNumber() throws IOException, ClientException {
        SmsDTO smsDTO = new SmsDTO();
        List<SmsDTO> smsDTOList = Lists.newArrayList(smsDTO);
        JDYRecordDTO jdyRecordDTO = new JDYRecordDTO();
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        String message = "message";
        queue.add(message);

        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        when(parserService.getSmsDTOList(anyString())).thenReturn(smsDTOList);
        when(parserService.getJDYRecordDTO(eq(message), anyString())).thenReturn(jdyRecordDTO);

        fixture.processQueue();

        verify(parserService, times(1)).getJDYRecordDTO(anyString(), errorMessageCaptor.capture());
        verify(aliyunSMSAdapter, times(0)).sendMessage(any(SmsDTO.class));
        verify(jdyRecordService, times(1)).updateRecordStatus(eq(jdyRecordDTO));

        assertTrue(errorMessageCaptor.getValue().length() > 0);

    }

    @Test
    public void testProcessQueueClientException() throws ClientException, IOException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        String errCode = "errCode";
        String errMsg = "errMsg";
        ClientException clientException = new ClientException(errCode, errMsg);
        JDYRecordDTO jdyRecordDTO = new JDYRecordDTO();
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add("message");
        when(parserService.getSmsDTOList(anyString())).thenReturn(Lists.newArrayList(smsDTO));
        when(parserService.getJDYRecordDTO(anyString(), anyString())).thenReturn(jdyRecordDTO);
        doThrow(clientException).when(aliyunSMSAdapter).sendMessage(any(SmsDTO.class));
        doNothing().when(jdyRecordService).updateRecordStatus(any(JDYRecordDTO.class));

        fixture.processQueue();

        verify(aliyunSMSAdapter, times(1)).sendMessage(aliyunAdapterCaptor.capture());
        verify(logService, times(1)).logFailure(logServiceCaptor.capture());
        verify(emailService, times(1)).send(emailSubject.capture(), emailText.capture());
        verify(jdyRecordService, times(1)).updateRecordStatus(jdyRecordDTOCaptor.capture());

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
        assertEquals(jdyRecordDTO, jdyRecordDTOCaptor.getValue());
    }

    @Test
    public void testProcessQueueSendSmsException() throws ClientException, IOException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        String errMsg = "failure message";
        SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setMessage(errMsg);
        SendSmsException sendSmsException = new SendSmsException(sendSmsResponse);
        JDYRecordDTO jdyRecordDTO = new JDYRecordDTO();
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add("message");
        when(parserService.getSmsDTOList(anyString())).thenReturn(Lists.newArrayList(smsDTO));
        when(parserService.getJDYRecordDTO(anyString(), anyString())).thenReturn(jdyRecordDTO);
        doThrow(sendSmsException).when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());
        doNothing().when(jdyRecordService).updateRecordStatus(any(JDYRecordDTO.class));

        fixture.processQueue();

        verify(aliyunSMSAdapter, times(1)).sendMessage(aliyunAdapterCaptor.capture());
        verify(logService, times(1)).logFailure(logServiceCaptor.capture());
        verify(emailService, times(1)).send(emailSubject.capture(), emailText.capture());
        verify(jdyRecordService, times(1)).updateRecordStatus(jdyRecordDTOCaptor.capture());

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
        assertEquals(jdyRecordDTO, jdyRecordDTOCaptor.getValue());
    }

    @Test
    public void testProcessGeneralException() throws ClientException, IOException {
        String phoneNumber = "12356";
        String templateCode = "templateCode";
        String params = "params";
        SmsDTO smsDTO = new SmsDTO(phoneNumber, templateCode, params);
        String errMsg = "exception message";
        Exception exception = new RuntimeException(errMsg);
        JDYRecordDTO jdyRecordDTO = new JDYRecordDTO();
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add("message");
        when(parserService.getSmsDTOList(anyString())).thenReturn(Lists.newArrayList(smsDTO));
        when(parserService.getJDYRecordDTO(anyString(), anyString())).thenReturn(jdyRecordDTO);
        doThrow(exception).when(aliyunSMSAdapter).sendMessage(aliyunAdapterCaptor.capture());
        doNothing().when(jdyRecordService).updateRecordStatus(any(JDYRecordDTO.class));

        fixture.processQueue();

        verify(aliyunSMSAdapter, times(1)).sendMessage(aliyunAdapterCaptor.capture());
        verify(logService, times(1)).logFailure(logServiceCaptor.capture());
        verify(emailService, times(1)).send(emailSubject.capture(), emailText.capture());
        verify(jdyRecordService, times(1)).updateRecordStatus(jdyRecordDTOCaptor.capture());

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
        assertEquals(jdyRecordDTO, jdyRecordDTOCaptor.getValue());
    }

    @Test
    public void testCheckBlocking() {
        Queue<String> queue = (Queue<String>) ReflectionTestUtils.getField(fixture, "messageQueue");
        Integer threshold = 2;
        ReflectionTestUtils.setField(fixture, "blockingThreshold", threshold);
        queue.add("1");
        queue.add("2");
        queue.add("3");

        fixture.checkBlocking();

        verify(emailService, times(1)).send(anyString(), anyString());
    }
}
