package org.theabconline.smsservice.mapping;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegistrationFormTest {

    private RegistrationForm fixture;

    @Before
    public void setup() {
        fixture = new RegistrationForm();
    }

    @Test
    public void testGetPathDefault() {
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getFieldsPath());
    }

    @Test
    public void testGetPathDefaultWithEmpty() {
        fixture.setFieldsPath("");
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getFieldsPath());
    }

    @Test
    public void testGetPathWithRoot() {
        fixture.setFieldsPath("/");
        assertEquals("", fixture.getFieldsPath());
    }

    @Test
    public void testGetPathCustomized() {
        String customPath = "custom";

        fixture.setFieldsPath(customPath);

        assertEquals(customPath, fixture.getFieldsPath());
    }
}
