package org.theabconline.smsservice.service;

import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.SmsExceptionDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class LogService {

    private static final String LOG_FILE = "/root/workspace/abc-sms-service/FailedMessages.csv";

    public void logFailure(SmsExceptionDTO smsExceptionDTO) {
        FileWriter fileWriter = null;

        try {
            File logFile = new File(LOG_FILE);
            fileWriter = new FileWriter(logFile, true);
            fileWriter.append(smsExceptionDTO.getPhoneNumber()).append(";")
                    .append(smsExceptionDTO.getTemplateCode()).append(";")
                    .append(smsExceptionDTO.getParams()).append(";")
                    .append(smsExceptionDTO.getErrorMessage())
                    .append("\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter == null) {
                return;
            }
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
