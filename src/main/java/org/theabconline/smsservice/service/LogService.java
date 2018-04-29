package org.theabconline.smsservice.service;

import org.springframework.stereotype.Service;
import org.theabconline.smsservice.dto.SmsExceptionDTO;
import org.theabconline.smsservice.dto.UserRegistrationFailureDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class LogService {

    private static final String SMS_LOG_FILE = "/root/workspace/abc-sms-service/FailedMessages.csv";
    private static final String REGISTRATION_LOG_FILE = "/root/workspace/abc-sms-service/FailedRegistration.csv";

    public void logFailure(SmsExceptionDTO smsExceptionDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append(smsExceptionDTO.getPhoneNumber()).append(";")
                .append(smsExceptionDTO.getTemplateCode()).append(";")
                .append(smsExceptionDTO.getParams()).append(";")
                .append(smsExceptionDTO.getErrorMessage())
                .append("\n");
        writeToFile(SMS_LOG_FILE, sb.toString());
    }

    public void logFailure(UserRegistrationFailureDTO userRegistrationFailureDTO) {
        StringBuilder sb = new StringBuilder();
        sb.append(userRegistrationFailureDTO.getUserId()).append(";")
                .append(userRegistrationFailureDTO.getName()).append(";")
                .append(userRegistrationFailureDTO.getDepartment()).append(";")
                .append(userRegistrationFailureDTO.getEmail()).append(";")
                .append(userRegistrationFailureDTO.getMobile()).append(";")
                .append(userRegistrationFailureDTO.getErrorMessage())
                .append("\n");
        writeToFile(REGISTRATION_LOG_FILE, sb.toString());
    }

    private void writeToFile(String file, String content) {
        FileWriter fileWriter = null;

        try {
            File logFile = new File(file);
            fileWriter = new FileWriter(logFile, true);
            fileWriter.append(content);
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
