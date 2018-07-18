package org.theabconline.smsservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JDYRecordDTO;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class JDYRecordServiceTest {

    @InjectMocks
    private JDYRecordService fixture;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fixture, "apiUrl", "https://www.jiandaoyun.com/api/v1/");
        ReflectionTestUtils.setField(fixture, "apiSecret", "1");
    }

    @Test
    public void updateRecordStatus() {
        String appId = "appId";
        String entryId = "entryId";
        String dataId = "dataId";
        String messageSentWidget = "messageSentWidget";
        boolean isMessageSent = true;
        String errorMessageWidget = "errorMessageWidget";
        String errorMessage = "errorMessage";
        JDYRecordDTO jdyRecordDTO = new JDYRecordDTO(appId, entryId, dataId, messageSentWidget, isMessageSent, errorMessageWidget, errorMessage);

        fixture.updateRecordStatus(jdyRecordDTO);

        ArgumentCaptor<String> requestUrlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> httpMethodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        Mockito.verify(restTemplate, Mockito.times(1)).exchange(requestUrlCaptor.capture(), httpMethodCaptor.capture(), httpEntityCaptor.capture(), Mockito.eq(String.class));
        assertEquals("https://www.jiandaoyun.com/api/v1/app/appId/entry/entryId/data_update", requestUrlCaptor.getValue());
        assertEquals(HttpMethod.POST, httpMethodCaptor.getValue());
        HttpEntity<?> httpEntity = httpEntityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, httpEntity.getHeaders().getContentType());
        assertEquals(JDYRecordService.BEARER + "1", httpEntity.getHeaders().get(JDYRecordService.AUTHORIZATION_HEADER).get(0));
        assertEquals(jdyRecordDTO, httpEntity.getBody());
    }

    @Test
    public void buildRequestUrl() {
        String requestUrl = fixture.buildRequestUrl("{appId}", "{entryId}");
        String expectedRequestUrl = "https://www.jiandaoyun.com/api/v1/app/{appId}/entry/{entryId}/data_update";
        assertEquals(expectedRequestUrl, requestUrl);
    }
}