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

    private RestTemplate restTemplate;

    private ParsingService parsingService;

    @Autowired
    public JdyService(RestTemplate restTemplate,
                      ParsingService parsingService) {
        this.restTemplate = restTemplate;
        this.parsingService = parsingService;
    }

    public String updateRecordMessage(RecordBO recordBO, String message) {
        String requestUrl = buildRequestUrl(recordBO.getAppId(), recordBO.getEntryId());
        JdyRecordDTO jdyRecordDTO = getPayload(recordBO, message);
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<?> httpEntity = new HttpEntity<Object>(jdyRecordDTO, headers);

        ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.POST, httpEntity, String.class);

        return response.getBody();
    }

    String buildRequestUrl(String appId, String entryId) {
        return apiUrl + String.format("app/%s/entry/%s/data_update", appId, entryId);
    }

    HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION_HEADER, BEARER + apiSecret);
        return headers;
    }

    JdyRecordDTO getPayload(RecordBO recordBO, String message) {
        JdyRecordDTO jdyRecordDTO = new JdyRecordDTO();
        jdyRecordDTO.setData_id(recordBO.getDataId());
        Map<String, Map<String, String>> data = Maps.newHashMap();
        Map<String, String> fieldValue = Maps.newHashMap();
        fieldValue.put("value", message);
        String messageWidgetName = parsingService.getMessageWidget(recordBO.getAppId(), recordBO.getEntryId());
        data.put(messageWidgetName, fieldValue);
        jdyRecordDTO.setData(data);
        return jdyRecordDTO;
    }

}
