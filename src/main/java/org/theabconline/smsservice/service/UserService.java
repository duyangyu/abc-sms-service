package org.theabconline.smsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.AccessTokenDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.dto.UserRegistrationResponseDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@EnableScheduling
public class UserService {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static final String CORP_ID_KEY = "corpid";
    public static final String CORP_SECRET_KEY = "corpsecret";
    public static final String ACCESS_TOKEN_KEY = "access_token";

    @Value("${aliyun.corpid}")
    private String corpId;

    @Value("${aliyun.corpsecret")
    private String corpSecret;

    @Value("${wechat.departmentId:2}")
    private Integer departmentId;

    @Value("${wechat.accessTokenAPI}")
    private String accessTokenUrl;

    @Value("${wechat.creatUserAPI}")
    private String createUserUrl;

    private final ParserService parserService;

    private final EmailService emailService;

    private final LogService logService;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private final Queue<UserRegistrationDTO> messageQueue;

    private String accessToken;

    private Long expirationTime;

    @Autowired
    public UserService(ParserService parserService,
                       RestTemplate restTemplate,
                       EmailService emailService,
                       LogService logService,
                       ObjectMapper objectMapper) {
        this.parserService = parserService;
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.logService = logService;
        this.objectMapper = objectMapper;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void createUser(String message) {
        try {
            UserRegistrationDTO userRegistrationDTO = parserService.getUserParams(message);
            userRegistrationDTO.setUserId(String.valueOf(System.currentTimeMillis())); // this should be ok for now since we only have 1 instance
            userRegistrationDTO.setDepartment(departmentId);
        } catch (IOException e) {
            handleParsingException(message);
        }
    }

    @Scheduled(fixedDelayString = "${process.fixedDelay:5000}", initialDelay = 0)
    public void processQueue() throws JsonProcessingException {
        UserRegistrationDTO userRegistrationDTO = messageQueue.poll();
        if (userRegistrationDTO != null) {
            try {
                updateAccessTokenIfNecessary();
            } catch (Exception e) {
                emailService.send("Failed to update access token", objectMapper.writeValueAsString(userRegistrationDTO));
                logService.logFailure(userRegistrationDTO);
                LOGGER.debug("Failed to create user due to unable to update access token, user: {}", objectMapper.writeValueAsString(userRegistrationDTO));
                return;
            }
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put(ACCESS_TOKEN_KEY, accessToken);
            ResponseEntity<UserRegistrationResponseDTO> responseEntity = restTemplate.postForEntity(createUserUrl, userRegistrationDTO, UserRegistrationResponseDTO.class, requestParams);
            if (responseEntity.getStatusCodeValue() != 200 || responseEntity.getBody().getErrcode() != 0) {
                emailService.send("Failed to create user", objectMapper.writeValueAsString(userRegistrationDTO));
                logService.logFailure(userRegistrationDTO);
                LOGGER.error("Failed to create user due api call failure, error message: {}", responseEntity.getBody().getErrmsg());
                LOGGER.debug("User: {}", objectMapper.writeValueAsString(userRegistrationDTO));
            }
        }

    }

    private void updateAccessTokenIfNecessary() {
        if (accessToken != null && System.currentTimeMillis() < expirationTime) {
            return;
        }
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(CORP_ID_KEY, corpId);
        requestParams.put(CORP_SECRET_KEY, corpSecret);
        AccessTokenDTO accessTokenDTO = restTemplate.getForEntity(accessTokenUrl, AccessTokenDTO.class, requestParams).getBody();
        if (accessTokenDTO == null || accessTokenDTO.getAccess_token() == null ||
                accessTokenDTO.getErrcode() != 0 || !"ok".equals(accessTokenDTO.getErrmsg())) {
            LOGGER.error("Failed to update access token for creating new user");
            throw new RuntimeException("Failed to update access token for creating new user");
        }
        this.accessToken = accessTokenDTO.getAccess_token();
        Long expireInMillis = accessTokenDTO.getExpires_in() * 1000; // unit of expires_in field is second
        this.expirationTime = System.currentTimeMillis() + expireInMillis - 10 * 1000; // put 10 seconds buffer
        LOGGER.debug("Access token updated, token: {}, expire time: {}", accessToken, expirationTime);
    }

    private void handleParsingException(String message) {
        String subject = "Error! Failed to parse user registration message from JianDaoYun";
        String text = "Payload: \n" + message;
        emailService.send(subject, text);
        LOGGER.info("Sent parsing error email notification");
    }
}
