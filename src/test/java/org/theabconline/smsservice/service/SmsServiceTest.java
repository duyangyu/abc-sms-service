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
import org.theabconline.smsservice.entity.RawMessageBO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.entity.SmsMessageBO;
import org.theabconline.smsservice.entity.SmsRequestBO;
import org.theabconline.smsservice.mapping.FieldMapping;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.RawMessageRepository;
import org.theabconline.smsservice.repository.RecordRepository;
import org.theabconline.smsservice.repository.SmsMessageRepository;
import org.theabconline.smsservice.repository.SmsRequestRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class SmsServiceTest {

    @InjectMocks
    private SmsService fixture;

    @Mock
    private RawMessageRepository rawMessageRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private SmsRequestRepository smsRequestRepository;

    @Mock
    private SmsMessageRepository smsMessageRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private ParsingService parsingService;

    @Mock
    private AliyunSMSAdapter aliyunSMSAdapter;

    @Mock
    private EmailService emailService;


    @Test
    public void testSaveMessageHappyPath() {
        String message = "message";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce";
        String sha1 = "sha1";
        ArgumentCaptor<RawMessageBO> rawMessageBOArgumentCaptor = ArgumentCaptor.forClass(RawMessageBO.class);
        when(validationService.isValid(eq(message), eq(timestamp), eq(nonce), eq(sha1))).thenReturn(true);

        fixture.saveMessage(message, timestamp, nonce, sha1);

        verify(rawMessageRepository, times(1)).save(rawMessageBOArgumentCaptor.capture());
        RawMessageBO rawMessageBO = rawMessageBOArgumentCaptor.getValue();
        assertEquals(message, rawMessageBO.getMessage());
        assertFalse(rawMessageBO.getProcessed());
    }

    @Test
    public void testSaveMessageInvalidMessage() {
        String message = "message";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce";
        String sha1 = "sha1";
        when(validationService.isValid(eq(message), eq(timestamp), eq(nonce), eq(sha1))).thenReturn(false);

        try {
            fixture.saveMessage(message, timestamp, nonce, sha1);
        } catch (Exception e) {
            assertEquals("Invalid Message", e.getMessage());
        }

        verify(rawMessageRepository, times(0)).save(any(RawMessageBO.class));
    }

    @Test
    public void testProcessRawMessages() {
        String message = "message";
        RawMessageBO rawMessageBO = createRawMessageBO(message);
        SmsService fixtureSpy = spy(fixture);
        ArgumentCaptor<List> rawMessageBOListArgumentCaptor = ArgumentCaptor.forClass(List.class);
        when(rawMessageRepository.getRawMessageBOSByIsProcessedFalse()).thenReturn(Lists.newArrayList(rawMessageBO));
        doNothing().when(fixtureSpy).processRawMessage(eq(rawMessageBO));

        fixtureSpy.processRawMessages();

        verify(fixtureSpy, times(1)).processRawMessage(eq(rawMessageBO));
        verify(rawMessageRepository, times(1)).save(rawMessageBOListArgumentCaptor.capture());
        assertEquals(1, rawMessageBOListArgumentCaptor.getValue().size());
        RawMessageBO updatedRawMessageBO = (RawMessageBO) rawMessageBOListArgumentCaptor.getValue().get(0);
        assertEquals(message, updatedRawMessageBO.getMessage());
        assertTrue(updatedRawMessageBO.getProcessed());
    }

    @Test
    public void testProcessRawMessageHappyPath() throws IOException {
        Long rawMessageId = 1L;
        String message = "message";
        RawMessageBO rawMessageBO = createRawMessageBO(rawMessageId, message);
        String appId = "appId";
        String entryId = "entryId";
        String dataId = "dataId";
        FormMetadata formMetadata = new FormMetadata();
        SmsTemplate smsTemplate = new SmsTemplate();
        formMetadata.setSmsTemplates(Lists.newArrayList(smsTemplate));
        ArgumentCaptor<RecordBO> recordBOArgumentCaptor = ArgumentCaptor.forClass(RecordBO.class);
        when(parsingService.getAppId(eq(message))).thenReturn(appId);
        when(parsingService.getEntryId(eq(message))).thenReturn(entryId);
        when(parsingService.getDataId(eq(message))).thenReturn(dataId);
        when(parsingService.getFormMetadata(eq(message))).thenReturn(formMetadata);
        SmsService fixtureSpy = spy(fixture);

        fixtureSpy.processRawMessage(rawMessageBO);

        verify(recordRepository, times(1)).save(recordBOArgumentCaptor.capture());
        RecordBO recordBO = recordBOArgumentCaptor.getValue();
        assertEquals(rawMessageId, recordBO.getRawMessageId());
        assertEquals(appId, recordBO.getAppId());
        assertEquals(entryId, recordBO.getEntryId());
        assertEquals(dataId, recordBO.getDataId());
        assertNull(recordBO.getErrorMessage());
        verify(fixtureSpy, times(1)).processSmsRequest(eq(message), eq(recordBO), eq(smsTemplate));
    }

    @Test
    public void testProcessRawMessageWithParsingException() throws IOException {
        Long rawMessageId = 1L;
        String message = "message";
        RawMessageBO rawMessageBO = createRawMessageBO(rawMessageId, message);
        String appId = "appId";
        String entryId = "entryId";
        FormMetadata formMetadata = new FormMetadata();
        SmsTemplate smsTemplate = new SmsTemplate();
        formMetadata.setSmsTemplates(Lists.newArrayList(smsTemplate));
        String exceptionMessage = "exception message";
        Exception parsingException = new IOException(exceptionMessage);
        ArgumentCaptor<RecordBO> recordBOArgumentCaptor = ArgumentCaptor.forClass(RecordBO.class);
        when(parsingService.getAppId(eq(message))).thenReturn(appId);
        when(parsingService.getEntryId(eq(message))).thenReturn(entryId);
        when(parsingService.getDataId(eq(message))).thenThrow(parsingException);
        SmsService fixtureSpy = spy(fixture);

        fixtureSpy.processRawMessage(rawMessageBO);

        verify(recordRepository, times(1)).save(recordBOArgumentCaptor.capture());
        RecordBO recordBO = recordBOArgumentCaptor.getValue();
        assertEquals(rawMessageId, recordBO.getRawMessageId());
        assertEquals(appId, recordBO.getAppId());
        assertEquals(entryId, recordBO.getEntryId());
        assertNull(recordBO.getDataId());
        assertEquals(exceptionMessage, recordBO.getErrorMessage());
        verify(fixtureSpy, times(1)).handleParsingFailed(eq(parsingException), eq(message));
        verify(emailService, times(1)).send(anyString(), anyString());
        verify(fixtureSpy, times(0)).processSmsRequest(anyString(), any(RecordBO.class), any(SmsTemplate.class));
    }

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
        SmsService fixtureSpy = spy(fixture);
        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenReturn(payload);
        when(aliyunSMSAdapter.sendMessage(any(SmsRequestDTO.class))).thenReturn(sendSmsResponse);
        when(sendSmsResponse.getCode()).thenReturn("OK");
        when(sendSmsResponse.getBizId()).thenReturn(bizId);
        ArgumentCaptor<SmsRequestDTO> smsRequestDTOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestDTO.class);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);
        doNothing().when(fixtureSpy).saveSmsMessages(any(SmsRequestBO.class));

        fixtureSpy.processSmsRequest(rawMessage, recordBO, smsTemplate);

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
        verify(fixtureSpy, times(1)).saveSmsMessages(eq(smsRequestBO));
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
        SmsService fixtureSpy = spy(fixture);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);
        String errorMessage = "error message";
        IOException parsingException = new IOException(errorMessage);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenThrow(parsingException);

        fixtureSpy.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertFalse(smsRequestBO.getSent());
        assertEquals(errorMessage, smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        assertNull(smsRequestBO.getPayload());
        verify(aliyunSMSAdapter, times(0)).sendMessage(any(SmsRequestDTO.class));
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
        SmsService fixtureSpy = spy(fixture);
        String errorMessage = "error message";
        ClientException clientException = new ClientException(errorMessage);
        when(parsingService.getPhoneNumbers(eq(rawMessage), eq(phoneNumbersWidget))).thenReturn(phoneNumbers);
        when(parsingService.getPayload(eq(rawMessage), eq(fieldMappings))).thenReturn(payload);
        when(aliyunSMSAdapter.sendMessage(any(SmsRequestDTO.class))).thenThrow(clientException);

        ArgumentCaptor<SmsRequestDTO> smsRequestDTOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestDTO.class);
        ArgumentCaptor<SmsRequestBO> smsRequestBOArgumentCaptor = ArgumentCaptor.forClass(SmsRequestBO.class);
        doNothing().when(fixtureSpy).saveSmsMessages(any(SmsRequestBO.class));

        fixtureSpy.processSmsRequest(rawMessage, recordBO, smsTemplate);

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
        verify(fixtureSpy, times(1)).saveSmsMessages(eq(smsRequestBO));
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
        SmsService fixtureSpy = spy(fixture);
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
        doNothing().when(fixtureSpy).saveSmsMessages(any(SmsRequestBO.class));

        fixtureSpy.processSmsRequest(rawMessage, recordBO, smsTemplate);

        verify(aliyunSMSAdapter, times(1)).sendMessage(smsRequestDTOArgumentCaptor.capture());
        SmsRequestDTO smsRequestDTO = smsRequestDTOArgumentCaptor.getValue();
        assertEquals(phoneNumbers, smsRequestDTO.getPhoneNumber());
        assertEquals(templateCode, smsRequestDTO.getTemplateCode());
        assertEquals(payload, smsRequestDTO.getParams());
        verify(fixtureSpy, times(1)).handleSendingFailed(eq(phoneNumbers), eq(templateCode), eq(payload), eq(errorMessage));
        verify(smsRequestRepository, times(1)).save(smsRequestBOArgumentCaptor.capture());
        SmsRequestBO smsRequestBO = smsRequestBOArgumentCaptor.getValue();
        assertNull(smsRequestBO.getBizId());
        assertEquals(templateCode, smsRequestBO.getTemplateCode());
        assertEquals(phoneNumbers, smsRequestBO.getPhoneNumbers());
        assertEquals(payload, smsRequestBO.getPayload());
        assertFalse(smsRequestBO.getSent());
        assertEquals(errorMessage, smsRequestBO.getErrorMessage());
        assertEquals(recordId, smsRequestBO.getRecordId());
        verify(fixtureSpy, times(1)).saveSmsMessages(eq(smsRequestBO));
    }

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

    private RawMessageBO createRawMessageBO(String message) {
        return createRawMessageBO(null, message);
    }

    private RawMessageBO createRawMessageBO(Long id, String message) {
        RawMessageBO rawMessageBO = new RawMessageBO();
        rawMessageBO.setId(id);
        rawMessageBO.setMessage(message);
        rawMessageBO.setProcessed(false);
        return rawMessageBO;
    }

}
