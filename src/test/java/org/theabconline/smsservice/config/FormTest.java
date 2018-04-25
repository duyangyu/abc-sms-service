package org.theabconline.smsservice.config;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

public class FormTest {

    private static final String DEFAULT_PATH_FIELD_NAME = "defaultPath";
    private static final String DEFAULT_PATH = "/default";

    private Form fixture;

    @Before
    public void setup() {
        fixture = new Form();
        ReflectionTestUtils.setField(fixture, DEFAULT_PATH_FIELD_NAME, DEFAULT_PATH);
    }

    @Test
    public void testGetPathDefault() {
        assertEquals(DEFAULT_PATH, fixture.getPhoneNumberPath());
    }

    @Test
    public void testGetPathDefaultWithEmpty() {
        fixture.setPhoneNumberPath("");
        assertEquals(DEFAULT_PATH, fixture.getPhoneNumberPath());
    }


    @Test
    public void testGetPathCustomized() {
        String customPath = "custom";

        fixture.setPhoneNumberPath(customPath);

        assertEquals(customPath, fixture.getPhoneNumberPath());
    }

    @Test
    public void testGetFormId() {
        String appId = "appId";
        String entryId = "entryId";
        fixture.setAppId(appId);
        fixture.setEntryId(entryId);

        assertEquals(appId + entryId, fixture.getFormId());
    }
}
