package org.theabconline.smsservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
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

        fixture.send(subject, text);

        Mockito.verify(javaMailSender, Mockito.times(1)).send(argumentCaptor.capture());
        assertEquals(subject, argumentCaptor.getValue().getSubject());
        assertEquals(text, argumentCaptor.getValue().getText());
        assertNotNull(argumentCaptor.getValue().getTo());
    }
}
