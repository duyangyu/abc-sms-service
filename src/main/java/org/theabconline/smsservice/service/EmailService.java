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

    @Value("${email.notification.recipients}")
    private String recipients;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendSendingFailureEmail(SmsVO smsVO, String errorMessage) {
        String subject = "Error! Failed to send sms to" + smsVO.getPhoneNumber();
        StringBuilder sb = new StringBuilder();
        sb.append("Recipient(s): ").append(smsVO.getPhoneNumber()).append("\n")
                .append("Template code: ").append(smsVO.getTemplateCode()).append("\n")
                .append("Payload: ").append(smsVO.getParams()).append("\n")
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
