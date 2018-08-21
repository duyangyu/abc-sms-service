package org.theabconline.smsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class SchedulerService {

    private final RecordService recordService;

    private final SmsRequestService smsRequestService;

    @Value("${process.maxCount:4}")
    Integer maxCount;

    @Autowired
    public SchedulerService(RecordService recordService, SmsRequestService smsRequestService) {
        this.recordService = recordService;
        this.smsRequestService = smsRequestService;
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelay = 0)
    public void processRawMessages() {
        recordService.processRawMessages();
    }

    @Scheduled(fixedDelayString = "${checkBlocking.fixedDelay:10000}", initialDelay = 0)
    public void checkBlocking() {
        recordService.checkBlocking();
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterTenMinutes() {
        Long millis = 1000L * 60 * 10;
        smsRequestService.updateRequestStatus(maxCount, millis);
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterOneHour() {
        Long millis = 1000L * 60 * 60;
        smsRequestService.updateRequestStatus(maxCount, millis);
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterSevenHours() {
        Long millis = 1000L * 60 * 60 * 7;
        smsRequestService.updateRequestStatus(maxCount, millis);
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterSixteenHours() {
        Long millis = 1000L * 60 * 60 * 16;
        smsRequestService.updateRequestStatus(maxCount, millis);
    }
}
