package org.theabconline.smsservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class EmailServiceTest {

    @InjectMocks
    EmailService fixture;

    @Mock
    JavaMailSender javaMailSender;

    @Captor
    ArgumentCaptor<SimpleMailMessage> argumentCaptor;

    @Test
    public void testSend() {
        String subject = "subject";
        String text = "text";
        String sender = "sender";
        String[] recipients = new String[]{"recipients"};
        ReflectionTestUtils.setField(fixture, "sender", sender);
        ReflectionTestUtils.setField(fixture, "recipients", recipients);

        fixture.send(subject, text);

        verify(javaMailSender, times(1)).send(argumentCaptor.capture());
        assertEquals(subject, argumentCaptor.getValue().getSubject());
        assertEquals(text, argumentCaptor.getValue().getText());
        assertTrue(Arrays.equals(recipients, argumentCaptor.getValue().getTo()));
        assertEquals(sender, argumentCaptor.getValue().getFrom());
    }
}
