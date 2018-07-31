package org.theabconline.smsservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JdyRecordDTO;
import org.theabconline.smsservice.entity.RecordBO;

import javax.mail.internet.ContentType;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class JdyServiceTest {

    @InjectMocks
    private JdyService fixture;

    @Mock
    private ParsingService parsingService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fixture, "apiUrl", "https://www.jiandaoyun.com/api/v1/");
        ReflectionTestUtils.setField(fixture, "apiSecret", "1");
    }

    @Test
    public void testBuildRequestUrl() {
        String apiUrl = "apiUrl";
        String appId = "appId";
        String entryId = "entryId";
        ReflectionTestUtils.setField(fixture, "apiUrl", apiUrl);

        String requestUrl = fixture.buildRequestUrl(appId, entryId);
        String expectedResult = apiUrl + String.format("app/%s/entry/%s/data_update", appId, entryId);

        assertEquals(expectedResult, requestUrl);
    }

    @Test
    public void testGetHttpHeaders() {
        String apiSecret = "apiSecret";
        ReflectionTestUtils.setField(fixture, "apiSecret", apiSecret);

        HttpHeaders httpHeaders = fixture.getHttpHeaders();

        assertEquals(MediaType.APPLICATION_JSON, httpHeaders.getContentType());
        assertEquals(JdyService.BEARER + apiSecret, httpHeaders.getValuesAsList(JdyService.AUTHORIZATION_HEADER).get(0));

    }

    @Test
    public void testGetPayload() {
        String dataId = "dataId";
        String appId = "appId";
        String entryId = "entryId";
        RecordBO recordBO = new RecordBO();
        recordBO.setDataId(dataId);
        recordBO.setAppId(appId);
        recordBO.setEntryId(entryId);
        String message = "message";
        String messageWidgetName = "messageWidgetName";
        when(parsingService.getMessageWidget(appId, entryId)).thenReturn(messageWidgetName);

        JdyRecordDTO jdyRecordDTO = fixture.getPayload(recordBO, message);

        assertEquals(dataId, jdyRecordDTO.getData_id());
        assertEquals(message, jdyRecordDTO.getData().get(messageWidgetName).get("value"));
    }

    @Test
    public void testUpdateRecordMessage() {
        String requestUrl = "requestUrl";
        String message = "message";
        String appId = "appId";
        String entryId = "entryId";
        RecordBO recordBO = new RecordBO();
        recordBO.setAppId(appId);
        recordBO.setEntryId(entryId);
        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO();
        HttpHeaders httpHeaders = new HttpHeaders();
        ResponseEntity responseEntityMock = mock(ResponseEntity.class);
        ArgumentCaptor<HttpEntity> httpEntityArgumentCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        JdyService fixtureSpy = spy(fixture);
        doReturn(requestUrl).when(fixtureSpy).buildRequestUrl(eq(appId), eq(entryId));
        doReturn(jdyRecordDTO).when(fixtureSpy).getPayload(eq(recordBO), eq(message));
        doReturn(httpHeaders).when(fixtureSpy).getHttpHeaders();
        when(responseEntityMock.getBody()).thenReturn("responseBody");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class))).thenReturn(responseEntityMock);

        fixtureSpy.updateRecordMessage(recordBO, message);

        verify(restTemplate, times(1)).exchange(eq(requestUrl), eq(HttpMethod.POST), httpEntityArgumentCaptor.capture(), eq(String.class));
        HttpEntity httpEntity = httpEntityArgumentCaptor.getValue();
        assertEquals(httpHeaders, httpEntity.getHeaders());
        assertEquals(jdyRecordDTO, httpEntity.getBody());
    }
}