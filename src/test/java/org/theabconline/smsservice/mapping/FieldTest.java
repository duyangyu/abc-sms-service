package org.theabconline.smsservice.mapping;

import org.junit.Before;
import org.junit.Test;

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
    public void testGetPathWithRoot() {
        fixture.setPath("/");
        assertEquals("", fixture.getPath());
    }

    @Test
    public void testGetPathCustomized() {
        String customPath = "custom";

        fixture.setPath(customPath);

        assertEquals(customPath, fixture.getPath());
    }
}
