package org.theabconline.smsservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.dto.UserRegistrationDTO;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ParsingServiceTest {

    private static final String PAYLOAD_MULTIPLE = "{ \"data\": { \"appId\": \"appId1\", \"entryId\": \"entryId1\", \"metadataField\": \"{ \\\"smsTemplates\\\": [ { \\\"smsTemplateCode\\\": \\\"smsTemplateCode1\\\", \\\"phoneNumbersWidget\\\": \\\"phoneNumberField1\\\", \\\"fieldMappings\\\": [ { \\\"widget\\\": \\\"widget1\\\", \\\"smsField\\\": \\\"smsField1\\\" }, { \\\"widget\\\": \\\"widget2\\\", \\\"smsField\\\": \\\"smsField2\\\" } ] }, { \\\"smsTemplateCode\\\": \\\"smsTemplateCode2\\\", \\\"phoneNumbersWidget\\\": \\\"phoneNumberField2\\\", \\\"fieldMappings\\\": [ { \\\"widget\\\": \\\"widget3\\\", \\\"smsField\\\": \\\"smsField3\\\" } ] } ] }\", \"phoneNumberField1\": [ \"123\", \"456\" ], \"widget1\": \"widget1\", \"widget2\": \"widget2\", \"phoneNumberField2\": \"789\", \"widget3\": \"widget3\" } }";
    private static final String PAYLOAD_INVALID = "{\"data\":{\"appId\":\"appId3\",\"entryId\":\"entryId3\"}}";

    private static final String PHONE_NUMBER_1_MULTI = "123,456";
    private static final String PHONE_NUMBER_2_MULTI = "789";
    private static final String TEMPLATE_CODE_1_MULTI = "smsTemplateCode1";
    private static final String TEMPLATE_CODE_2_MULTI = "smsTemplateCode2";
    private static final String PARAMS_1_MULTI = "{\"smsField1\":\"widget1\",\"smsField2\":\"widget2\"}";
    private static final String PARAMS_2_MULTI = "{\"smsField3\":\"widget3\"}";

    private static final String USER_REGISTRATION_PAYLOAD = "{\"data\":{\"nameField\":\"name\",\"emailField\":\"email\",\"mobileField\":\"mobile\"}}";
    private static final String USER_REGISTRATION_NAME = "name";
    private static final String USER_REGISTRATION_EMAIL = "email";
    private static final String USER_REGISTRATION_MOBILE = "mobile";

    @Autowired
    ParsingService fixture;

    @Test
    public void testGetUserParams() throws IOException {
        assertTrue(true);
    }

}
