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
    private ArgumentCaptor<SmsDTO> smsVOCaptor1;

    @Captor
    private ArgumentCaptor<SmsDTO> smsVOCaptor2;

    @Captor
    private ArgumentCaptor<SmsDTO> smsVOCaptor3;

    @Captor
    private ArgumentCaptor<String> errorMessageCaptor1;

    @Captor
    private ArgumentCaptor<String> errorMessageCaptor2;

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
        Mockito.doNothing().when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        assertEquals(smsDTO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
    }

    @Test
    public void testProcessQueueClientException() throws ClientException {
        SmsDTO smsDTO = new SmsDTO();
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errCode = "errCode";
        String errMsg = "errMsg";
        ClientException clientException = new ClientException(errCode, errMsg);
        Mockito.doThrow(clientException).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendSendingFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsDTO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsDTO, smsVOCaptor2.getValue());
        assertEquals(errMsg, errorMessageCaptor1.getValue());
        assertEquals(smsDTO, smsVOCaptor3.getValue());
        assertEquals(errMsg, errorMessageCaptor2.getValue());
    }

    @Test
    public void testProcessQueueSendSmsException() throws ClientException {
        SmsDTO smsDTO = new SmsDTO();
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errMsg = "failure message";
        SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setMessage(errMsg);
        SendSmsException sendSmsException = new SendSmsException(sendSmsResponse);
        Mockito.doThrow(sendSmsException).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendSendingFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsDTO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsDTO, smsVOCaptor2.getValue());
        assertEquals(errMsg, errorMessageCaptor1.getValue());
        assertEquals(smsDTO, smsVOCaptor3.getValue());
        assertEquals(errMsg, errorMessageCaptor2.getValue());
    }

    @Test
    public void testProcessGeneralException() throws ClientException {
        SmsDTO smsDTO = new SmsDTO();
        Queue<SmsDTO> queue = (Queue<SmsDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        queue.add(smsDTO);
        String errMsg = "exception message";
        Exception exception = new RuntimeException(errMsg);
        Mockito.doThrow(exception).when(aliyunSMSAdapter).sendMessage(smsVOCaptor1.capture());

        fixture.processQueue();

        Mockito.verify(logService, Mockito.times(1)).logFailure(smsVOCaptor2.capture(), errorMessageCaptor1.capture());
        Mockito.verify(emailService, Mockito.times(1)).sendSendingFailureEmail(smsVOCaptor3.capture(), errorMessageCaptor2.capture());

        assertEquals(smsDTO, smsVOCaptor1.getValue());
        assertEquals(0, queue.size());
        assertEquals(smsDTO, smsVOCaptor2.getValue());
        assertTrue(errorMessageCaptor1.getValue().contains(errMsg));
        assertEquals(smsDTO, smsVOCaptor3.getValue());
        assertTrue(errorMessageCaptor2.getValue().contains(errMsg));
    }
}
