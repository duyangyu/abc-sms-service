package org.theabconline.smsservice.service;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.JDYRecordDTO;
import org.theabconline.smsservice.dto.MessageStatus;
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.SmsExceptionDTO;
import org.theabconline.smsservice.exception.SendSmsException;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@EnableScheduling
public class SMSService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    private final ValidationService validationService;

    private final ParserService parserService;

    private final AliyunSMSAdapter aliyunSMSAdapter;

    private final EmailService emailService;

    private final LogService logService;

    private final JDYRecordService jdyRecordService;

    private final SMSTrackingService smsTrackingService;

    private final Queue<String> messageQueue;

    @Value("${checkBlocking.threshold:10}")
    private Integer blockingThreshold;

    @Autowired
    public SMSService(ValidationService validationService,
                      ParserService parserService,
                      AliyunSMSAdapter aliyunSMSAdapter,
                      EmailService emailService,
                      LogService logService,
                      JDYRecordService jdyRecordService,
                      SMSTrackingService smsTrackingService) {
        this.validationService = validationService;
        this.parserService = parserService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
        this.emailService = emailService;
        this.logService = logService;
        this.jdyRecordService = jdyRecordService;
        this.smsTrackingService = smsTrackingService;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void send(String message, String timestamp, String nonce, String sha1) {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.error("Validation failed, timestamp: {}, nonce: {}, sha1: {}", timestamp, nonce, sha1);
            LOGGER.error("message: {}", message);
            throw new RuntimeException("Invalid Message");
        }

        messageQueue.add(message);
        LOGGER.debug("SMS message: {}", message);
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelay = 0)
    public void processQueue() {
        String message = messageQueue.poll();

        if (message == null) {
            return;
        }

        List<SmsDTO> smsDTOList = Lists.newArrayList();
        String appId = null;
        String entryId = null;
        String dataId = null;
        try {
            smsDTOList = parserService.getSmsDTOList(message);
            appId = parserService.getAppId(message);
            entryId = parserService.getEntryId(message);
            dataId = parserService.getDataId(message);
        } catch (Exception e) {
            String stacktraceString = Throwables.getStackTraceAsString(e);
            handleParsingException(message + stacktraceString);
            LOGGER.error(stacktraceString);
        }

        for (SmsDTO smsDTO : smsDTOList) {
            if (Strings.isNullOrEmpty(smsDTO.getPhoneNumber())) {
                LOGGER.error("Processed SMS message without phone number: {}", smsDTO.getParams());
                continue;
            }
            String errorText = null;
            SendSmsResponse sendSmsResponse = null;
            try {
                sendSmsResponse = aliyunSMSAdapter.sendMessage(smsDTO);
                LOGGER.info("Message sent, to: {}, template: {}, payload: {}", smsDTO.getPhoneNumber(), smsDTO.getTemplateCode(), smsDTO.getParams());
            } catch (ClientException e) {
                errorText = e.getErrMsg();
                LOGGER.error("Message not sent: {}", errorText);
                handleSendingException(smsDTO, errorText);
            } catch (SendSmsException e) {
                errorText = e.getSendSmsResponse().getMessage();
                LOGGER.error("Message not sent: {}", errorText);
                handleSendingException(smsDTO, errorText);
            } catch (Exception e) {
                errorText = Throwables.getStackTraceAsString(e);
                LOGGER.error("Message not sent: {}", errorText);
                handleSendingException(smsDTO, errorText);
            }

            if (sendSmsResponse == null) {
                return;
            }

            if (sendSmsResponse.getCode().equals("OK")) {
                smsTrackingService.addSuccess(appId, entryId, dataId, smsDTO.getTemplateCode(), sendSmsResponse.getBizId(), smsDTO.getPhoneNumber().split(","));
            } else {
                smsTrackingService.addFailure(appId, entryId, dataId, smsDTO.getTemplateCode(), sendSmsResponse.getBizId(), smsDTO.getPhoneNumber().split(","), errorText);
            }
        }
    }

    @Scheduled(fixedDelayString = "${checkBlocking.fixedDelay:10000}", initialDelay = 0)
    public void checkBlocking() {
        if (messageQueue.size() > blockingThreshold) {
            sendQueueBlockingEmail();
        }
    }

    private void handleParsingException(String message) {
        String subject = "Error! Failed to parse message from JianDaoYun";
        String text = "Payload: \n" + message;
        emailService.send(subject, text);
        LOGGER.info("Sent parsing error email notification");
    }

    private void handleSendingException(SmsDTO smsDTO, String errorMessage) {
        logError(smsDTO, errorMessage);
        LOGGER.info("SMS delivery failure logged");

        sendNotificationEmail(smsDTO, errorMessage);
        LOGGER.info("SMS delivery failure email notification sent");
    }

    private void logError(SmsDTO smsDTO, String errorMessage) {
        logService.logFailure(new SmsExceptionDTO(smsDTO, errorMessage));
    }

    private void sendNotificationEmail(SmsDTO smsDTO, String errorMessage) {
        String subject = "Error! Failed to send sms to " + smsDTO.getPhoneNumber();
        String text = "Recipient(s): " + smsDTO.getPhoneNumber() + "\n" +
                "Template code: " + smsDTO.getTemplateCode() + "\n" +
                "Payload: " + smsDTO.getParams() + "\n" +
                "\n" +
                "Error message: " + errorMessage;
        emailService.send(subject, text);
    }

    private void sendQueueBlockingEmail() {
        String subject = "Warning! SMS message queue size is larger than threshold, please check for potential issue";
        String text = "";

        emailService.send(subject, text);
        LOGGER.info("Sent sms queue blocking email notification");
    }

}
