package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
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

    private static final String USER_REGISTRATION_PAYLOAD = "{\"data\":{\"nameField\":\"name\",\"emailField\":\"email\",\"mobileField\":\"mobile\"}}";
    private static final String USER_REGISTRATION_NAME = "name";
    private static final String USER_REGISTRATION_EMAIL = "email";
    private static final String USER_REGISTRATION_MOBILE = "mobile";


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
    public void testGetSmsParamsMultipleRecipients() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<SmsDTO> voList = fixture.getSmsParams(PAYLOAD_MULTIPLE);

        assertEquals(2, voList.size());
        SmsDTO result1 = voList.get(0);
        assertEquals(PHONE_NUMBER_1_MULTI, result1.getPhoneNumber());
        assertEquals(TEMPLATE_CODE_1_MULTI, result1.getTemplateCode());
        JsonNode expectedJson = objectMapper.readTree(PARAMS_1_MULTI);
        JsonNode actualJson = objectMapper.readTree(result1.getParams());
        assertTrue(expectedJson.equals(actualJson));
        SmsDTO result2 = voList.get(1);
        assertEquals(PHONE_NUMBER_2_MULTI, result2.getPhoneNumber());
        assertEquals(TEMPLATE_CODE_2_MULTI, result2.getTemplateCode());
        assertEquals(PARAMS_2_MULTI, result2.getParams());
    }

    @Test
    public void testGetUserParams() throws IOException {
        UserRegistrationDTO dto = fixture.getUserParams(USER_REGISTRATION_PAYLOAD);

        assertEquals(USER_REGISTRATION_NAME, dto.getName());
        assertEquals(USER_REGISTRATION_EMAIL, dto.getEmail());
        assertEquals(USER_REGISTRATION_MOBILE, dto.getMobile());
    }
}
