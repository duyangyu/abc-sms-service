package org.theabconline.smsservice.service;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.theabconline.smsservice.entity.RawMessageBO;
import org.theabconline.smsservice.entity.RecordBO;
import org.theabconline.smsservice.mapping.FormMetadata;
import org.theabconline.smsservice.mapping.SmsTemplate;
import org.theabconline.smsservice.repository.RawMessageRepository;
import org.theabconline.smsservice.repository.RecordRepository;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class RecordServiceTest {

    @InjectMocks
    private RecordService fixture;

    @Mock
    private RawMessageRepository rawMessageRepository;

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private ValidationService validationService;

    @Mock
    private ParsingService parsingService;

    @Mock
    private SmsRequestService smsRequestService;

    @Mock
    private ErrorHandlingService errorHandlingService;


    @Test
    public void testSaveMessageHappyPath() {
        String message = "message";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = "nonce";
        String sha1 = "sha1";
        ArgumentCaptor<RawMessageBO> rawMessageBOArgumentCaptor = ArgumentCaptor.forClass(RawMessageBO.class);
        when(validationService.isValid(eq(message), eq(timestamp), eq(nonce), eq(sha1))).thenReturn(true);

        fixture.saveRawMessage(message, timestamp, nonce, sha1);

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
            fixture.saveRawMessage(message, timestamp, nonce, sha1);
        } catch (Exception e) {
            assertEquals("Invalid Message", e.getMessage());
        }

        verify(rawMessageRepository, times(0)).save(any(RawMessageBO.class));
    }

    @Test
    public void testProcessRawMessages() {
        String message = "message";
        RawMessageBO rawMessageBO = createRawMessageBO(message);
        RecordService fixtureSpy = spy(fixture);
        ArgumentCaptor<RawMessageBO> rawMessageBOArgumentCaptor = ArgumentCaptor.forClass(RawMessageBO.class);
        when(rawMessageRepository.getRawMessageBOSByIsProcessedFalse()).thenReturn(Lists.newArrayList(rawMessageBO));
        doNothing().when(fixtureSpy).processRawMessage(eq(rawMessageBO));

        fixtureSpy.processRawMessages();

        verify(fixtureSpy, times(1)).processRawMessage(eq(rawMessageBO));
        verify(rawMessageRepository, times(1)).save(rawMessageBOArgumentCaptor.capture());
        RawMessageBO updatedRawMessageBO = rawMessageBOArgumentCaptor.getValue();
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

        fixture.processRawMessage(rawMessageBO);

        verify(recordRepository, times(1)).save(recordBOArgumentCaptor.capture());
        RecordBO recordBO = recordBOArgumentCaptor.getValue();
        assertEquals(rawMessageId, recordBO.getRawMessageId());
        assertEquals(appId, recordBO.getAppId());
        assertEquals(entryId, recordBO.getEntryId());
        assertEquals(dataId, recordBO.getDataId());
        assertNull(recordBO.getErrorMessage());
        verify(smsRequestService, times(1)).processSmsRequest(eq(message), eq(recordBO), eq(smsTemplate));
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

        fixture.processRawMessage(rawMessageBO);

        verify(recordRepository, times(1)).save(recordBOArgumentCaptor.capture());
        RecordBO recordBO = recordBOArgumentCaptor.getValue();
        assertEquals(rawMessageId, recordBO.getRawMessageId());
        assertEquals(appId, recordBO.getAppId());
        assertEquals(entryId, recordBO.getEntryId());
        assertNull(recordBO.getDataId());
        assertEquals(exceptionMessage, recordBO.getErrorMessage());
        verify(errorHandlingService, times(1)).handleParsingFailed(eq(parsingException), eq(message));
        verify(smsRequestService, times(0)).processSmsRequest(anyString(), any(RecordBO.class), any(SmsTemplate.class));
    }

    @Test
    public void testGetUnprocessedCount() {
        Integer blockingThreshold = 2;
        Long actualNumber = blockingThreshold + 1L;
        ReflectionTestUtils.setField(fixture, "blockingThreshold", blockingThreshold);
        when(rawMessageRepository.countByIsProcessedFalse()).thenReturn(actualNumber);

        fixture.checkBlocking();

        verify(rawMessageRepository, times(1)).countByIsProcessedFalse();
        verify(errorHandlingService, times(1)).handleBlocking(eq(actualNumber));
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
