package org.theabconline.smsservice.config;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

public class FieldTest {

    private static final String DEFAULT_PATH_FIELD_NAME = "defaultPath";
    private static final String DEFAULT_PATH = "/default";

    private Field fixture;

    @Before
    public void setup() {
        fixture = new Field();
        ReflectionTestUtils.setField(fixture, DEFAULT_PATH_FIELD_NAME, DEFAULT_PATH);
    }

    @Test
    public void testGetPathDefault() {
        assertEquals(DEFAULT_PATH, fixture.getPath());
    }

    @Test
    public void testGetPathCustomized() {
        String customPath = "custom";

        fixture.setPath(customPath);

        assertEquals(customPath, fixture.getPath());
    }
}
