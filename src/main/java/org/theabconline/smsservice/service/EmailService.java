package org.theabconline.smsservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${email.notification.recipients:duyangyu@theabconline.org}")
    private String[] recipients;

    @Value("${spring.mail.username:smsservice@mail2.theabconline.org}")
    private String sender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void send(String subject, String text) {
        LOGGER.debug("Sending mail, recipients: {}, subject: {}, text: {}", recipients, subject, text);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(recipients);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
            LOGGER.debug("Mail sent");
        } catch (Exception e) {
            LOGGER.error("Failed to send mail, recipients: {}, subject: {}, text: {}", recipients, subject, text);
            LOGGER.debug("Reason: {}", e.getCause().getMessage());
        }

    }
}
