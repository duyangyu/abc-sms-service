package org.theabconline.smsservice.service;

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JdyRecordDTO;

@Service
public class JdyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdyService.class);

    static final String BEARER = "Bearer ";
    static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${jdyun.api.url}")
    private String apiUrl;

    @Value("${jdyun.api.secret}")
    private String apiSecret;

    @Value("${jdyun.report.table.appId}")
    private String reportFormAppId;

    @Value("${jdyun.report.table.entryId}")
    private String reportFormEntryId;

    private RestTemplate restTemplate;

    private ErrorHandlingService errorHandlingService;

    @Autowired
    public JdyService(RestTemplate restTemplate,
                      ErrorHandlingService errorHandlingService) {
        this.restTemplate = restTemplate;
        this.errorHandlingService = errorHandlingService;
    }

    String createReportRecord(JdyRecordDTO payload) {
        return makeRequest(payload, getInsertRequestUrl());
    }

    String updateRecordMessage(JdyRecordDTO payload) {
        return makeRequest(payload, getUpdateRequestUrl());
    }

    String makeRequest(JdyRecordDTO payload, String url) {
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(payload, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        } catch (Exception e) {
            LOGGER.error(Throwables.getStackTraceAsString(e));
            return null;
        }
        if (!response.getStatusCode().is2xxSuccessful()) {
            errorHandlingService.handleJdyFailure(response.getBody());
            LOGGER.error("Http status: {}, response body: {}", response.getStatusCode(), response.getBody());
            return null;
        }

        return response.getBody();
    }

    String getUpdateRequestUrl() {
        return apiUrl + String.format("app/%s/entry/%s/data_update", reportFormAppId, reportFormEntryId);
    }

    String getInsertRequestUrl() {
        return apiUrl + String.format("app/%s/entry/%s/data_create", reportFormAppId, reportFormEntryId);
    }

    HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION_HEADER, BEARER + apiSecret);
        return headers;
    }

}
