package org.theabconline.smsservice.service;

import com.aliyuncs.exceptions.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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

    private final Queue<SmsVO> messageQueue;

    @Autowired
    public SMSService(ValidationService validationService,
                      ParserService parserService,
                      AliyunSMSAdapter aliyunSMSAdapter,
                      EmailService emailService,
                      LogService logService) {
        this.validationService = validationService;
        this.parserService = parserService;
        this.aliyunSMSAdapter = aliyunSMSAdapter;
        this.emailService = emailService;
        this.logService = logService;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void send(String message, String timestamp, String nonce, String sha1) throws IOException {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.error("Validation failed, timestamp: {}, nonce: {}, sha1: {}", timestamp, nonce, sha1);
            LOGGER.error("message: {}", message);
            throw new RuntimeException("Invalid Message");
        }

        List<SmsVO> smsVOList = parserService.getSmsParams(message);

        messageQueue.addAll(smsVOList);
        LOGGER.debug("Added {} message(s) to queue", smsVOList.size());
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelay = 0)
    public void processQueue() {
        SmsVO smsVO = messageQueue.poll();
        if (smsVO != null) {
            try {
                aliyunSMSAdapter.sendMessage(smsVO);
                LOGGER.info("Send message to {}, template: {}, payload: {}", smsVO.getPhoneNumber(), smsVO.getTemplateCode(), smsVO.getParams());
                return;
            } catch (ClientException e) {
                LOGGER.error("Failed to send message, caused by aliyun client, error message: {}", e.getErrMsg());
                handleError(smsVO, e.getErrMsg());
            } catch (SendSmsException e) {
                LOGGER.error("Failed to send message, caused by invalid payload, error message: {}", e.getSendSmsResponse().getMessage());
                handleError(smsVO, e.getSendSmsResponse().getMessage());
            } catch (Exception e) {
                LOGGER.error("Failed to send message, unknown reason");
                handleError(smsVO, e.toString());
            }
        }
        LOGGER.info("Processed queue");
    }

    @Scheduled(fixedDelayString = "${checkBlocking.fixedDelay:10000}", initialDelay = 0)
    public void checkBlocking() {
        if (messageQueue.size() > 10) {
            emailService.sendQueueBlockingEmail();
        }
    }

    private void handleError(SmsVO smsVO, String errorMessage) {
        logService.logFailure(smsVO, errorMessage);
        emailService.sendFailureEmail(smsVO, errorMessage);
    }

}
