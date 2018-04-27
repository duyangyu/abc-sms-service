package org.theabconline.smsservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.dto.SmsDTO;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
@EnableAutoConfiguration
public class ParserServiceTest {

    private static final String PAYLOAD_SINGLE = "{\"data\":{\"appId\":\"appId2\",\"entryId\":\"entryId2\",\"phoneNumberField3\":\"789\",\"field4\":\"field4\"}}";
    private static final String PAYLOAD_MULTIPLE = "{\"data\":{\"appId\":\"appId1\",\"entryId\":\"entryId1\",\"phoneNumberField1\":\"123\",\"phoneNumberField2\":\"456\",\"field1\":\"field1\",\"field2\":\"field2\",\"field3\":\"field3\"}}";

    private static final String PHONE_NUMBER_SINGLE = "789";
    private static final String TEMPLATE_CODE_SINGLE = "template3";
    private static final String PARAMS_SINGLE = "{\"key4\":\"field4\"}";

    private static final String PHONE_NUMBER_1_MULTI = "123";
    private static final String PHONE_NUMBER_2_MULTI = "456";
    private static final String TEMPLATE_CODE_1_MULTI = "template1";
    private static final String TEMPLATE_CODE_2_MULTI = "template2";
    private static final String PARAMS_1_MULTI = "{\"key1\":\"field1\",\"key2\":\"field2\"}";
    private static final String PARAMS_2_MULTI = "{\"key3\":\"field3\"}";

    @Autowired
    ParserService fixture;

    @Test
    public void testGetSmsParamsSingleRecipient() throws IOException {
        List<SmsDTO> voList = fixture.getSmsParams(PAYLOAD_SINGLE);

        assertEquals(1, voList.size());
        SmsDTO result = voList.get(0);
        assertEquals(PHONE_NUMBER_SINGLE, result.getPhoneNumber());
        assertEquals(TEMPLATE_CODE_SINGLE, result.getTemplateCode());
        assertEquals(PARAMS_SINGLE, result.getParams());
    }

    @Test
    public void testGetSimsParamsMultipleRecipients() throws IOException {
        List<SmsDTO> voList = fixture.getSmsParams(PAYLOAD_MULTIPLE);

        assertEquals(2, voList.size());
        SmsDTO result1 = voList.get(0);
        assertEquals(PHONE_NUMBER_1_MULTI, result1.getPhoneNumber());
        assertEquals(TEMPLATE_CODE_1_MULTI, result1.getTemplateCode());
        assertEquals(PARAMS_1_MULTI, result1.getParams());
        SmsDTO result2 = voList.get(1);
        assertEquals(PHONE_NUMBER_2_MULTI, result2.getPhoneNumber());
        assertEquals(TEMPLATE_CODE_2_MULTI, result2.getTemplateCode());
        assertEquals(PARAMS_2_MULTI, result2.getParams());
    }
}
