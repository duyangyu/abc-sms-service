package org.theabconline.smsservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.SmsDTO;

@Service
public class EmailService {

    public static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${email.notification.recipients:duyangyu@theabconline.org}")
    private String recipients;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSendingFailureEmail(SmsDTO smsDTO, String errorMessage) {
        String subject = "Error! Failed to send sms to" + smsDTO.getPhoneNumber();
        StringBuilder sb = new StringBuilder();
        sb.append("Recipient(s): ").append(smsDTO.getPhoneNumber()).append("\n")
                .append("Template code: ").append(smsDTO.getTemplateCode()).append("\n")
                .append("Payload: ").append(smsDTO.getParams()).append("\n")
                .append("\n")
                .append("Error message: ").append(errorMessage);

        send(subject, sb.toString());
        LOGGER.info("Sent SMS delivery failure email notification");
    }

    public void sendParsingErroEmail(String payload) {
        String subject = "Error! Failed to parse message from JianDaoYun";
        String text = "Payload: \n" + payload;

        send(subject, text);
        LOGGER.info("Sent parsing error email notification");
    }

    public void sendQueueBlockingEmail() {
        String subject = "Warning! Message queue size is larger than threshold, please check for potential issue";
        String text = "";

        send(subject,text);
        LOGGER.info("Sent queue blocking email notification");
    }

    private void send(String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipients);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
