package org.theabconline.smsservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
public class JdyServiceTest {

    private static final String API_SECRET = "APISecret";
    private static final String API_URL = "apiUrl";
    private static final String APP_ID = "appId";
    private static final String ENTRY_ID = "entryId";

    @InjectMocks
    private JdyService fixture;

    @Mock
    private ParsingService parsingService;

    @Mock
    private RestTemplate restTemplate;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fixture, "apiUrl", "https://www.jiandaoyun.com/api/v1/");
        ReflectionTestUtils.setField(fixture, "apiSecret", API_SECRET);
        ReflectionTestUtils.setField(fixture, "apiUrl", API_URL);
        ReflectionTestUtils.setField(fixture, "reportFormAppId", APP_ID);
        ReflectionTestUtils.setField(fixture, "reportFormEntryId", ENTRY_ID);
    }

    @Test
    public void testGetUpdateRequestUrl() {
        String requestUrl = fixture.getUpdateRequestUrl();
        String expectedResult = API_URL + String.format("app/%s/entry/%s/data_update", APP_ID, ENTRY_ID);

        assertEquals(expectedResult, requestUrl);
    }

    @Test
    public void testGetInsertRequestUrl() {
        String requestUrl = fixture.getInsertRequestUrl();
        String expectedResult = API_URL + String.format("app/%s/entry/%s/data_create", APP_ID, ENTRY_ID);

        assertEquals(expectedResult, requestUrl);
    }

    @Test
    public void testGetHttpHeaders() {
        HttpHeaders httpHeaders = fixture.getHttpHeaders();

        assertEquals(MediaType.APPLICATION_JSON, httpHeaders.getContentType());
        assertEquals(JdyService.BEARER + API_SECRET, httpHeaders.getValuesAsList(JdyService.AUTHORIZATION_HEADER).get(0));
    }

}