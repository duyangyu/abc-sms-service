package org.theabconline.smsservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JdyRecordDTO;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
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
        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO(appId, entryId, dataId, messageSentWidget, isMessageSent, errorMessageWidget, errorMessage);

        fixture.updateRecordStatus(jdyRecordDTO);

        String requestUrlString = "https://www.jiandaoyun.com/api/v1/app/appId/entry/entryId/data_update";
        ArgumentCaptor<HttpEntity> httpEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate, times(1)).exchange(eq(requestUrlString), eq(HttpMethod.POST), httpEntityCaptor.capture(), eq(String.class));
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