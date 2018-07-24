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
import org.springframework.web.util.UriComponentsBuilder;
import org.theabconline.smsservice.dto.AccessTokenDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.dto.UserRegistrationFailureDTO;
import org.theabconline.smsservice.dto.UserRegistrationResponseDTO;
import org.theabconline.smsservice.exception.UpdateTokenException;
import org.theabconline.smsservice.exception.UserCreationException;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@EnableScheduling
public class UserService {

    public static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static final String CORP_ID_KEY = "corpid";
    public static final String CORP_SECRET_KEY = "corpsecret";
    public static final String ACCESS_TOKEN_KEY = "access_token";
    public static final String CREATED_MESSAGE = "created";
    public static final String UNABLE_TO_UPDATE_ACCESS_TOKEN_MESSAGE = "Unable to update access token";
    public static final String ACCESS_TOKEN_OK_MESSAGE = "ok";
    private final ParserService parserService;
    private final EmailService emailService;
    private final LogService logService;
    private final ValidationService validationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Queue<UserRegistrationDTO> messageQueue;
    @Value("${aliyun.corpid}")
    private String corpId;
    @Value("${aliyun.corpsecret}")
    private String corpSecret;
    @Value("${wechat.departmentId:2}")
    private Integer departmentId;
    @Value("${wechat.accessTokenAPI}")
    private String accessTokenUrl;
    @Value("${wechat.creatUserAPI}")
    private String createUserUrl;
    @Value("${checkBlocking.threshold:10}")
    private Integer blockingThreshold;
    private String accessToken;

    private Long expirationTime;

    @Autowired
    public UserService(ParserService parserService,
                       RestTemplate restTemplate,
                       EmailService emailService,
                       LogService logService,
                       ValidationService validationService, ObjectMapper objectMapper) {
        this.parserService = parserService;
        this.restTemplate = restTemplate;
        this.emailService = emailService;
        this.logService = logService;
        this.validationService = validationService;
        this.objectMapper = objectMapper;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void createUser(String message, String timestamp, String nonce, String sha1) {
        if (!validationService.isValid(message, timestamp, nonce, sha1)) {
            LOGGER.error("Validation failed, timestamp: {}, nonce: {}, sha1: {}", timestamp, nonce, sha1);
            LOGGER.error("message: {}", message);
            throw new RuntimeException("Invalid Message");
        }
        try {
            UserRegistrationDTO userRegistrationDTO = parserService.getUserParams(message);
            userRegistrationDTO.setUserid(String.valueOf(System.currentTimeMillis())); // this should be ok for now since we only have 1 instance
            userRegistrationDTO.setDepartment(departmentId);
            messageQueue.add(userRegistrationDTO);
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
                sendCreateUserRequest(userRegistrationDTO);
            } catch (UpdateTokenException e) {
                handleUpdateTokenException(userRegistrationDTO);
            } catch (UserCreationException e) {
                handleUserCreationException(userRegistrationDTO, e);
            }
        }
    }

    @Scheduled(fixedDelayString = "${checkBlocking.fixedDelay:10000}", initialDelay = 0)
    public void checkBlocking() {
        if (messageQueue.size() > blockingThreshold) {
            sendQueueBlockingEmail();
        }
    }

    private void handleUserCreationException(UserRegistrationDTO userRegistrationDTO, UserCreationException e) throws JsonProcessingException {
        String errorMessage = e.getMessage();
        String userRegistrationDTOString = objectMapper.writeValueAsString(userRegistrationDTO);
        String text = "Error message: " + errorMessage + "\n"
                + "payload: " + userRegistrationDTOString;
        emailService.send("Failed to create user", text);
        logService.logFailure(new UserRegistrationFailureDTO(userRegistrationDTO, errorMessage));
        LOGGER.error("Failed to create user, payload: {}, error message: {}", userRegistrationDTOString);
        LOGGER.debug("User: {}", userRegistrationDTOString);
    }

    private void handleUpdateTokenException(UserRegistrationDTO userRegistrationDTO) throws JsonProcessingException {
        emailService.send("Failed to update access token", objectMapper.writeValueAsString(userRegistrationDTO));
        logService.logFailure(new UserRegistrationFailureDTO(userRegistrationDTO, UNABLE_TO_UPDATE_ACCESS_TOKEN_MESSAGE));
        LOGGER.debug("Failed to create user due to unable to update access token, user: {}", objectMapper.writeValueAsString(userRegistrationDTO));
    }

    private void updateAccessTokenIfNecessary() throws JsonProcessingException {
        if (accessToken != null && System.currentTimeMillis() < expirationTime) {
            return;
        }
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(accessTokenUrl)
                .queryParam(CORP_ID_KEY, corpId)
                .queryParam(CORP_SECRET_KEY, corpSecret);
        AccessTokenDTO accessTokenDTO = restTemplate.getForEntity(urlBuilder.toUriString(),
                AccessTokenDTO.class).getBody();
        if (accessTokenDTO == null || accessTokenDTO.getAccess_token() == null ||
                accessTokenDTO.getErrcode() != 0 || !ACCESS_TOKEN_OK_MESSAGE.equals(accessTokenDTO.getErrmsg())) {
            LOGGER.error("Failed to update access token for creating new user: {}", objectMapper.writeValueAsString(accessTokenDTO));
            throw new UpdateTokenException("Failed to update access token for creating new user");
        }
        this.accessToken = accessTokenDTO.getAccess_token();
        Long expireInMillis = accessTokenDTO.getExpires_in() * 1000; // unit of expires_in field is second
        this.expirationTime = System.currentTimeMillis() + expireInMillis - 10 * 1000; // put 10 seconds buffer
        LOGGER.debug("Access token updated, token: {}, expire time: {}", accessToken, expirationTime);
    }

    private void sendCreateUserRequest(UserRegistrationDTO userRegistrationDTO) throws JsonProcessingException {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(createUserUrl)
                .queryParam(ACCESS_TOKEN_KEY, accessToken);
        ResponseEntity<UserRegistrationResponseDTO> responseEntity = restTemplate.postForEntity(urlBuilder.toUriString(),
                userRegistrationDTO,
                UserRegistrationResponseDTO.class);
        if (responseEntity.getStatusCodeValue() != 200 ||
                responseEntity.getBody().getErrcode() != 0 ||
                !CREATED_MESSAGE.equals(responseEntity.getBody().getErrmsg())) {
            throw new UserCreationException(responseEntity.getBody().getErrmsg());
        }
        LOGGER.info("User created, {}", objectMapper.writeValueAsString(userRegistrationDTO));
    }

    private void handleParsingException(String message) {
        String subject = "Error! Failed to parse user registration message from JianDaoYun";
        String text = "Payload: \n" + message;
        emailService.send(subject, text);
        LOGGER.debug("Sent parsing error email notification");
    }

    private void sendQueueBlockingEmail() {
        String subject = "Warning! User creation message queue size is larger than threshold, please check for potential issue";
        String text = "";

        emailService.send(subject, text);
        LOGGER.info("Sent user creation queue blocking email notification");
    }
}
