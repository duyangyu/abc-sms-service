package org.theabconline.smsservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.JdyRecordDTO;

@Service
public class JdyService {

    static final String BEARER = "Bearer ";
    static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("${jdyun.api.url}")
    private String apiUrl;

    @Value("${jdyun.api.secret}")
    private String apiSecret;

    private RestTemplate restTemplate;


    @Autowired
    public JdyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void updateRecord(JdyRecordDTO jdyRecordDTO) {
        String requestUrl = buildRequestUrl(jdyRecordDTO.getAppId(), jdyRecordDTO.getEntryId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION_HEADER, BEARER + apiSecret);
        HttpEntity<?> httpEntity = new HttpEntity<Object>(jdyRecordDTO, headers);
        restTemplate.exchange(requestUrl, HttpMethod.POST, httpEntity, String.class);
    }

    String buildRequestUrl(String appId, String entryId) {
        return apiUrl + String.format("app/%s/entry/%s/data_update", appId, entryId);
    }

}
