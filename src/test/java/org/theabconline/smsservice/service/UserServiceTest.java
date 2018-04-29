package org.theabconline.smsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.theabconline.smsservice.dto.AccessTokenDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.dto.UserRegistrationFailureDTO;
import org.theabconline.smsservice.dto.UserRegistrationResponseDTO;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceTest {

    private static final Integer DEPARTMENT_ID = 3;
    private static final String CREATE_USER_URL = "createUserUrl";
    private static final String CORP_ID = "corpId";
    private static final String CORP_SECRET = "corpSecret";
    private static final String ACCESS_TOKEN_URL = "accessTokenUrl";

    @InjectMocks
    private UserService fixture;

    @Mock
    private ParserService parserService;

    @Mock
    private EmailService emailService;

    @Mock
    private LogService logService;

    @Mock
    private ValidationService validationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private Queue<UserRegistrationDTO> messageQueue;

    @Before
    public void setup() {
        messageQueue = (Queue<UserRegistrationDTO>) ReflectionTestUtils.getField(fixture, "messageQueue");
        assertEquals(0, messageQueue.size());

        ReflectionTestUtils.setField(fixture, "departmentId", DEPARTMENT_ID);
        ReflectionTestUtils.setField(fixture, "createUserUrl", CREATE_USER_URL);
        ReflectionTestUtils.setField(fixture, "corpId", CORP_ID);
        ReflectionTestUtils.setField(fixture, "corpSecret", CORP_SECRET);
        ReflectionTestUtils.setField(fixture, "accessTokenUrl", ACCESS_TOKEN_URL);
    }

    @Test
    public void testCreateUserHappyPath() throws IOException {
        String name = "name";
        String email = "email";
        String mobile = "mobile";
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setName(name);
        userRegistrationDTO.setEmail(email);
        userRegistrationDTO.setMobile(mobile);
        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(parserService.getUserParams(Mockito.anyString())).thenReturn(userRegistrationDTO);

        fixture.createUser("message", "timestamp", "nonce", "sha1");

        Mockito.verify(validationService, Mockito.times(1)).isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        assertEquals(1, messageQueue.size());
        UserRegistrationDTO dto = messageQueue.poll();
        assertEquals(userRegistrationDTO, dto);
        assertEquals(DEPARTMENT_ID, userRegistrationDTO.getDepartment());
        assertNotNull(userRegistrationDTO.getUserId());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateUserInvalidInput() {
        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);

        fixture.createUser("message", "timestamp", "nonce", "sha1");

        fail("Exception expected");

    }

    @Test
    public void testCreateUserParsingException() throws IOException {
        String message = "message";
        Mockito.when(validationService.isValid(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(parserService.getUserParams(Mockito.anyString())).thenThrow(new IOException());

        fixture.createUser(message, "timestamp", "nonce", "sha1");

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyString(), messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains(message));
    }

    @Test
    public void testProcessQueueHappyPath() throws JsonProcessingException {
        String accessToken = "accessToken";
        ReflectionTestUtils.setField(fixture, "accessToken", accessToken);
        ReflectionTestUtils.setField(fixture, "expirationTime", System.currentTimeMillis() + 1000000);
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        messageQueue.add(userRegistrationDTO);
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO();
        responseDTO.setErrcode(0);
        responseDTO.setErrmsg(UserService.CREATED_MESSAGE);
        ResponseEntity<UserRegistrationResponseDTO> response = new ResponseEntity<>(responseDTO, HttpStatus.OK);
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(UserRegistrationDTO.class), (Class<Object>) Mockito.any(), Mockito.any(Map.class)))
                .thenReturn(response);

        fixture.processQueue();

        ArgumentCaptor<UserRegistrationDTO> dtoArgumentCaptor = ArgumentCaptor.forClass(UserRegistrationDTO.class);
        ArgumentCaptor<Map> requestParamsArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(restTemplate, Mockito.times(1)).postForEntity(Mockito.eq(CREATE_USER_URL), dtoArgumentCaptor.capture(), Mockito.eq(UserRegistrationResponseDTO.class), requestParamsArgumentCaptor.capture());
        assertEquals(0, messageQueue.size());
        assertEquals(userRegistrationDTO, dtoArgumentCaptor.getValue());
        Map<String, String> capturedRequestParams = requestParamsArgumentCaptor.getValue();
        assertEquals(accessToken, capturedRequestParams.get(UserService.ACCESS_TOKEN_KEY));
    }

    @Test
    public void testProcessQueueUpdateAccessTokenHappyPath() throws JsonProcessingException {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        messageQueue.add(userRegistrationDTO);
        String accessToken = "accessToken";
        ReflectionTestUtils.setField(fixture, "accessToken", accessToken);
        ReflectionTestUtils.setField(fixture, "expirationTime", System.currentTimeMillis() - 1000000);
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        String newAccessToken = "newAccessToken";
        Long expires_in = 7200L;
        accessTokenDTO.setAccess_token(newAccessToken);
        accessTokenDTO.setExpires_in(expires_in);
        accessTokenDTO.setErrcode(0);
        accessTokenDTO.setErrmsg(UserService.ACCESS_TOKEN_OK_MESSAGE);
        ArgumentCaptor<Map> requestParamsCaptor = ArgumentCaptor.forClass(Map.class);
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(accessTokenDTO, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(Mockito.eq(ACCESS_TOKEN_URL), Mockito.eq(AccessTokenDTO.class), requestParamsCaptor.capture()))
                .thenReturn(response);
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO();
        responseDTO.setErrcode(0);
        responseDTO.setErrmsg(UserService.CREATED_MESSAGE);
        ResponseEntity<UserRegistrationResponseDTO> registrationResponse = new ResponseEntity<>(responseDTO, HttpStatus.OK);
        Mockito.when(restTemplate.postForEntity(Mockito.anyString(), Mockito.any(UserRegistrationDTO.class), (Class<Object>) Mockito.any(), Mockito.any(Map.class)))
                .thenReturn(registrationResponse);

        fixture.processQueue();

        Map<String, String> requestParams = requestParamsCaptor.getValue();
        assertEquals(CORP_ID, requestParams.get(UserService.CORP_ID_KEY));
        assertEquals(CORP_SECRET, requestParams.get(UserService.CORP_SECRET_KEY));
        assertEquals(newAccessToken, ReflectionTestUtils.getField(fixture, "accessToken"));
        assertTrue(System.currentTimeMillis() < (Long) ReflectionTestUtils.getField(fixture, "expirationTime"));
        assertEquals(0, messageQueue.size());
    }

    @Test
    public void testProcessQueueUpdateAccessTokenException() throws JsonProcessingException {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        messageQueue.add(userRegistrationDTO);
        String accessToken = "accessToken";
        ReflectionTestUtils.setField(fixture, "accessToken", accessToken);
        ReflectionTestUtils.setField(fixture, "expirationTime", System.currentTimeMillis() - 1000000);
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        String newAccessToken = "newAccessToken";
        Long expires_in = 7200L;
        accessTokenDTO.setAccess_token(newAccessToken);
        accessTokenDTO.setExpires_in(expires_in);
        accessTokenDTO.setErrcode(123);
        accessTokenDTO.setErrmsg("errorMessage");
        ArgumentCaptor<Map> requestParamsCaptor = ArgumentCaptor.forClass(Map.class);
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(accessTokenDTO, HttpStatus.OK);
        Mockito.when(restTemplate.getForEntity(Mockito.eq(ACCESS_TOKEN_URL), Mockito.eq(AccessTokenDTO.class), requestParamsCaptor.capture()))
                .thenReturn(response);
        String emailContent = "update token failure email content";
        Mockito.when(objectMapper.writeValueAsString(Mockito.eq(userRegistrationDTO))).thenReturn(emailContent);

        fixture.processQueue();

        ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyString(), emailContentCaptor.capture());
        ArgumentCaptor<UserRegistrationFailureDTO> failureDTOArgumentCaptor = ArgumentCaptor.forClass(UserRegistrationFailureDTO.class);
        Mockito.verify(logService, Mockito.times(1)).logFailure(failureDTOArgumentCaptor.capture());
        Map<String, String> requestParams = requestParamsCaptor.getValue();
        assertEquals(CORP_ID, requestParams.get(UserService.CORP_ID_KEY));
        assertEquals(CORP_SECRET, requestParams.get(UserService.CORP_SECRET_KEY));
        assertEquals(0, messageQueue.size());
        assertEquals(emailContent, emailContentCaptor.getValue());
        assertEquals(UserService.UNABLE_TO_UPDATE_ACCESS_TOKEN_MESSAGE, failureDTOArgumentCaptor.getValue().getErrorMessage());
    }

    @Test
    public void testProcessQueueUserCreationException() throws JsonProcessingException {
        String accessToken = "accessToken";
        ReflectionTestUtils.setField(fixture, "accessToken", accessToken);
        ReflectionTestUtils.setField(fixture, "expirationTime", System.currentTimeMillis() + 1000000);
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        messageQueue.add(userRegistrationDTO);
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO();
        responseDTO.setErrcode(1);
        String userCreationErrorMessage = "error message";
        responseDTO.setErrmsg(userCreationErrorMessage);
        ResponseEntity<UserRegistrationResponseDTO> response = new ResponseEntity<>(responseDTO, HttpStatus.OK);
        ArgumentCaptor<Map> requestParamsCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.when(restTemplate.postForEntity(Mockito.eq(CREATE_USER_URL), Mockito.eq(userRegistrationDTO), Mockito.eq(UserRegistrationResponseDTO.class), requestParamsCaptor.capture()))
                .thenReturn(response);
        ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserRegistrationFailureDTO> failureLogCaptor = ArgumentCaptor.forClass(UserRegistrationFailureDTO.class);

        fixture.processQueue();

        Map<String, String> requestParams = requestParamsCaptor.getValue();
        assertEquals(0, messageQueue.size());
        assertEquals(accessToken, requestParams.get(UserService.ACCESS_TOKEN_KEY));
        Mockito.verify(emailService, Mockito.times(1)).send(Mockito.anyString(), emailContentCaptor.capture());
        Mockito.verify(logService, Mockito.times(1)).logFailure(failureLogCaptor.capture());
        assertTrue(emailContentCaptor.getValue().contains(userCreationErrorMessage));
        assertEquals(userCreationErrorMessage, failureLogCaptor.getValue().getErrorMessage());

    }
}
