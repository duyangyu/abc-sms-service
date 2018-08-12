package org.theabconline.smsservice.service;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JdyRecordDTO;
import org.theabconline.smsservice.entity.RecordBO;

import java.util.Map;

@Service
public class JdyService {

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

    private ParsingService parsingService;

    private ErrorHandlingService errorHandlingService;

    @Autowired
    public JdyService(RestTemplate restTemplate,
                      ParsingService parsingService,
                      ErrorHandlingService errorHandlingService) {
        this.restTemplate = restTemplate;
        this.parsingService = parsingService;
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

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            errorHandlingService.handleJdyFailure(response.getBody());
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

//    JdyRecordDTO getPayload(RecordBO recordBO, String message) {
//        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO();
//        jdyRecordDTO.setData_id(recordBO.getDataId());
//        Map<String, Map<String, String>> data = Maps.newHashMap();
//        Map<String, String> fieldValue = Maps.newHashMap();
//        fieldValue.put("value", message);
//        String messageWidgetName = parsingService.getMessageWidget(recordBO.getAppId(), recordBO.getEntryId());
//        data.put(messageWidgetName, fieldValue);
//        jdyRecordDTO.setData(data);
//        return jdyRecordDTO;
//    }

}
