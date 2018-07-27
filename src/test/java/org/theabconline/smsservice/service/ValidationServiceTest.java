package org.theabconline.smsservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ValidationServiceTest {

    private static final String SECRET = "supersecretkey";
    private static final String PAYLOAD = "{\"data\":{\"key\":\"value\"}}";
    private static final String TIMESTAMP = "1524706591684L";
    private static final String NONCE = "123";
    private static final String VALID_SHA1 = "f88da935abb8fe6b94b7270499a7e535dd7a37cc";
    private static final String INVALID_SHA1 = "invalidstuff";

    @InjectMocks
    ValidationService fixture;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fixture, "secret", SECRET);
    }

    @Test
    public void testIsValidTrue() {
        assertTrue(fixture.isValid(PAYLOAD, TIMESTAMP, NONCE, VALID_SHA1));
    }

    @Test
    public void testIsValidFalse() {
        assertFalse(fixture.isValid(PAYLOAD, TIMESTAMP, NONCE, INVALID_SHA1));
    }
}
