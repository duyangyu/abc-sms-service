package org.theabconline.smsservice.mapping;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FormTest {

    private Form fixture;

    @Before
    public void setup() {
        fixture = new Form();
    }

    @Test
    public void testGetPathDefault() {
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getPhoneNumberPath());
    }

    @Test
    public void testGetPathDefaultWithEmpty() {
        fixture.setPhoneNumberPath("");
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getPhoneNumberPath());
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
