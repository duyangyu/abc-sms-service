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

    @Value("${process.maxCount:3}")
    Integer maxCount;

    @Autowired
    public SchedulerService(RecordService recordService) {
        this.recordService = recordService;
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
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterOneHour() {
        Long millis = 1000L * 60 * 60;
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelayString = "${process.fixedDelay:5000}")
    public void updateStatusAfterOneDay() {
        Long millis = 1000L * 60 * 60 * 24;
    }
}
