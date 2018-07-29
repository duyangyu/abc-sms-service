package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.theabconline.smsservice.dto.SmsRequestDTO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.mapping.FieldMapping;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.SmsRequestRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class SmsRequestServiceTest {

    @InjectMocks
    private SmsRequestService fixture;

    @Mock
    private SmsRequestRepository smsRequestRepository;

    @Mock
    private ParsingService parsingService;

    @Mock
    private SmsMessageService smsMessageService;

    @Mock
    private ErrorHandlingService errorHandlingService;

    @Mock
    private AliyunSMSAdapter aliyunSMSAdapter;

    @Test
    public void testProcessSmsRequestHappyPath() throws IOException, ClientException {
        Long recordId = 1L;
        RecordBO recordBO = new RecordBO();
        recordBO.setId(recordId);
        String rawMessage = "raw message";
        String phoneNumbersWidget = "phone numbers widget";
        String phoneNumbers = "phone numbers";
        String templateCode = "template code";
        String payload = "payload";
        String bizId = "biz id";
        List<FieldMapping> fieldMappings = Lists.newArrayList();
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setPhoneNumbersWidget(phoneNumbersWidget);
        smsTemplate.setSmsTemplateCode(templateCode);
        smsTemplate.setFieldMappings(fieldMappings);
        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenReturn(payload);
        when(aliyunSMSAdapter.sendMessage(any(SmsRequestDTO.class))).thenReturn(sendSmsResponse);
        when(sendSmsResponse.getCode()).thenReturn("OK");
        when(sendSmsResponse.getBizId()).thenReturn(bizId);
        ArgumentCaptor<SmsRequestDTO> smsRequestDTOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestDTO.class);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);
        doNothing().when(smsMessageService).saveSmsMessages(any(SmsRequestBO.class));

        fixture.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(aliyunSMSAdapter, times(1)).sendMessage(smsRequestDTOArgumentCaptor.capture());
        SmsRequestDTO smsRequestDTO = smsRequestDTOArgumentCaptor.getValue();
        assertEquals(phoneNumbers, smsRequestDTO.getPhoneNumber());
        assertEquals(templateCode, smsRequestDTO.getTemplateCode());
        assertEquals(payload, smsRequestDTO.getParams());
        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertEquals(bizId, smsRequestBO.getBizId());
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertEquals(payload, smsRequestBO.getPayload());
        assertTrue(smsRequestBO.getSent());
        assertNull(smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        verify(smsMessageService, times(1)).saveSmsMessages(eq(smsRequestBO));
    }

    @Test
    public void testProcessSmsRequestNoPhoneNumber() throws IOException, ClientException {
        String rawMessage = "raw message";
        String phoneNumbersWidget = "phone numbers widget";
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setPhoneNumbersWidget(phoneNumbersWidget);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(" ");

        fixture.processSmsRequest(rawMessage, new RecordBO(), smsTemplate);

        verify(parsingService, times(0)).getPayload(anyString(), anyListOf(FieldMapping.class));
        verify(smsRequestRepository, times(0)).save(any(SmsRequestBO.class));
        verify(aliyunSMSAdapter, times(0)).sendMessage(any(SmsRequestDTO.class));
    }

    @Test
    public void processSmsRequestParsingException() throws IOException, ClientException {
        Long recordId = 1L;
        RecordBO recordBO = new RecordBO();
        recordBO.setId(recordId);
        String rawMessage = "raw message";
        String phoneNumbersWidget = "phone numbers widget";
        String phoneNumbers = "phone numbers";
        String templateCode = "template code";
        List<FieldMapping> fieldMappings = Lists.newArrayList();
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setPhoneNumbersWidget(phoneNumbersWidget);
        smsTemplate.setSmsTemplateCode(templateCode);
        smsTemplate.setFieldMappings(fieldMappings);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);
        String errorMessage = "error message";
        IOException parsingException = new IOException(errorMessage);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenThrow(parsingException);

        fixture.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertFalse(smsRequestBO.getSent());
        assertEquals(errorMessage, smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        assertNull(smsRequestBO.getPayload());
        verify(aliyunSMSAdapter, times(0)).sendMessage(any(SmsRequestDTO.class));
        verify(errorHandlingService, times(1)).handleParsingFailed(eq(parsingException), eq(rawMessage));
    }

    @Test
    public void processSmsRequestClientException() throws IOException, ClientException {
        Long recordId = 1L;
        RecordBO recordBO = new RecordBO();
        recordBO.setId(recordId);
        String rawMessage = "raw message";
        String phoneNumbersWidget = "phone numbers widget";
        String phoneNumbers = "phone numbers";
        String templateCode = "template code";
        String payload = "payload";
        List<FieldMapping> fieldMappings = Lists.newArrayList();
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setPhoneNumbersWidget(phoneNumbersWidget);
        smsTemplate.setSmsTemplateCode(templateCode);
        smsTemplate.setFieldMappings(fieldMappings);
        String errorMessage = "error message";
        ClientException clientException = new ClientException(errorMessage);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenReturn(payload);
        when(aliyunSMSAdapter.sendMessage(any(SmsRequestDTO.class))).thenThrow(clientException);

        ArgumentCaptor<SmsRequestDTO> smsRequestDTOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestDTO.class);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);

        fixture.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(aliyunSMSAdapter, times(1)).sendMessage(smsRequestDTOArgumentCaptor.capture());
        SmsRequestDTO smsRequestDTO = smsRequestDTOArgumentCaptor.getValue();
        assertEquals(phoneNumbers, smsRequestDTO.getPhoneNumber());
        assertEquals(templateCode, smsRequestDTO.getTemplateCode());
        assertEquals(payload, smsRequestDTO.getParams());
        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertNull(smsRequestBO.getBizId());
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertEquals(payload, smsRequestBO.getPayload());
        assertFalse(smsRequestBO.getSent());
        assertEquals(errorMessage, smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        verify(smsMessageService, times(1)).saveSmsMessages(eq(smsRequestBO));
    }

    @Test
    public void testProcessSmsRequestInvalidResponse() throws IOException, ClientException {
        Long recordId = 1L;
        RecordBO recordBO = new RecordBO();
        recordBO.setId(recordId);
        String rawMessage = "raw message";
        String phoneNumbersWidget = "phone numbers widget";
        String phoneNumbers = "phone numbers";
        String templateCode = "template code";
        String payload = "payload";
        List<FieldMapping> fieldMappings = Lists.newArrayList();
        SmsTemplate smsTemplate = new SmsTemplate();
        smsTemplate.setPhoneNumbersWidget(phoneNumbersWidget);
        smsTemplate.setSmsTemplateCode(templateCode);
        smsTemplate.setFieldMappings(fieldMappings);
        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenReturn(payload);
        when(aliyunSMSAdapter.sendMessage(any(SmsRequestDTO.class))).thenReturn(sendSmsResponse);
        String errorMessage = "error message";
        when(sendSmsResponse.getMessage()).thenReturn(errorMessage);
        when(sendSmsResponse.getCode()).thenReturn("NOT OK");
        when(sendSmsResponse.getBizId()).thenReturn(null);
        ArgumentCaptor<SmsRequestDTO> smsRequestDTOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestDTO.class);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);

        fixture.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(aliyunSMSAdapter, times(1)).sendMessage(smsRequestDTOArgumentCaptor.capture());
        SmsRequestDTO smsRequestDTO = smsRequestDTOArgumentCaptor.getValue();
        assertEquals(phoneNumbers, smsRequestDTO.getPhoneNumber());
        assertEquals(templateCode, smsRequestDTO.getTemplateCode());
        assertEquals(payload, smsRequestDTO.getParams());
        verify(errorHandlingService, times(1)).handleSendingFailed(eq(phoneNumbers), eq(templateCode), eq(payload), eq(errorMessage));
        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertNull(smsRequestBO.getBizId());
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertEquals(payload, smsRequestBO.getPayload());
        assertFalse(smsRequestBO.getSent());
        assertEquals(errorMessage, smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        verify(smsMessageService, times(1)).saveSmsMessages(eq(smsRequestBO));
    }

}
