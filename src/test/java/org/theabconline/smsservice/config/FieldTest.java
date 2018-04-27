package org.theabconline.smsservice.config;

import org.junit.Before;
import org.junit.Test;
import org.theabconline.smsservice.mapping.Field;
import org.theabconline.smsservice.mapping.FormMappings;

import static org.junit.Assert.assertEquals;

public class FieldTest {

    private Field fixture;

    @Before
    public void setup() {
        fixture = new Field();
    }

    @Test
    public void testGetPathDefault() {
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getPath());
    }

    @Test
    public void testGetPathDefaultWithEmpty() {
        fixture.setPath("");
        assertEquals(FormMappings.DEFAULT_PATH, fixture.getPath());
    }

    @Test
    public void testGetPathCustomized() {
        String customPath = "custom";

        fixture.setPath(customPath);

        assertEquals(customPath, fixture.getPath());
    }
}
