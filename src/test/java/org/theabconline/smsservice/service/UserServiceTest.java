package org.theabconline.smsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.theabconline.smsservice.dto.AccessTokenDTO;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.dto.UserRegistrationResponseDTO;

import java.io.IOException;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class UserServiceTest {

    private static final Integer DEPARTMENT_ID = 3;
    private static final String CREATE_USER_URL = "http://createUserUrl.com";
    private static final String CORP_ID = "corpId";
    private static final String CORP_SECRET = "corpSecret";
    private static final String ACCESS_TOKEN_URL = "http://accessTokenUrl.com";

    @InjectMocks
    private UserService fixture;

    @Mock
    private ParsingService parsingService;

    @Mock
    private EmailService emailService;

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

//    @Test
//    public void testCreateUserHappyPath() throws IOException {
//        String name = "name";
//        String email = "email";
//        String mobile = "mobile";
//        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
//        userRegistrationDTO.setName(name);
//        userRegistrationDTO.setEmail(email);
//        userRegistrationDTO.setMobile(mobile);
//        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(true);
//        when(parsingService.getUserParams(anyString())).thenReturn(userRegistrationDTO);
//
//        fixture.createUser("message", "timestamp", "nonce", "sha1");
//
//        verify(validationService, times(1)).isValid(anyString(), anyString(), anyString(), anyString());
//        assertEquals(1, messageQueue.size());
//        UserRegistrationDTO dto = messageQueue.poll();
//        assertEquals(userRegistrationDTO, dto);
//        assertEquals(DEPARTMENT_ID, userRegistrationDTO.getDepartment());
//        assertNotNull(userRegistrationDTO.getUserid());
//    }

    @Test(expected = RuntimeException.class)
    public void testCreateUserInvalidInput() {
        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        fixture.createUser("message", "timestamp", "nonce", "sha1");

        fail("Exception expected");

    }

//    @Test
//    public void testCreateUserParsingException() throws IOException {
//        String message = "message";
//        when(validationService.isValid(anyString(), anyString(), anyString(), anyString()))
//                .thenReturn(true);
//        when(parsingService.getUserParams(anyString())).thenThrow(new IOException());
//
//        fixture.createUser(message, "timestamp", "nonce", "sha1");
//
//        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
//        verify(emailService, times(1)).send(anyString(), messageCaptor.capture());
//        assertTrue(messageCaptor.getValue().contains(message));
//    }

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
        String url = UriComponentsBuilder.fromHttpUrl(CREATE_USER_URL).queryParam(UserService.ACCESS_TOKEN_KEY, accessToken).toUriString();
        ResponseEntity<UserRegistrationResponseDTO> response = new ResponseEntity<>(responseDTO, HttpStatus.OK);
        when(restTemplate.postForEntity(eq(url), eq(userRegistrationDTO), any(Class.class)))
                .thenReturn(response);

        fixture.processQueue();

        verify(restTemplate, times(1)).postForEntity(eq(url), eq(userRegistrationDTO), eq(UserRegistrationResponseDTO.class));
        assertEquals(0, messageQueue.size());
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
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(accessTokenDTO, HttpStatus.OK);
        String url = UriComponentsBuilder.fromHttpUrl(ACCESS_TOKEN_URL)
                .queryParam(UserService.CORP_ID_KEY, CORP_ID)
                .queryParam(UserService.CORP_SECRET_KEY, CORP_SECRET)
                .toUriString();
        when(restTemplate.getForEntity(eq(url), eq(AccessTokenDTO.class)))
                .thenReturn(response);
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO();
        responseDTO.setErrcode(0);
        responseDTO.setErrmsg(UserService.CREATED_MESSAGE);
        ResponseEntity<UserRegistrationResponseDTO> registrationResponse = new ResponseEntity<>(responseDTO, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(UserRegistrationDTO.class), any(Class.class)))
                .thenReturn(registrationResponse);

        fixture.processQueue();

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
        ResponseEntity<AccessTokenDTO> response = new ResponseEntity<>(accessTokenDTO, HttpStatus.OK);
        String url = UriComponentsBuilder.fromHttpUrl(ACCESS_TOKEN_URL)
                .queryParam(UserService.CORP_ID_KEY, CORP_ID)
                .queryParam(UserService.CORP_SECRET_KEY, CORP_SECRET)
                .toUriString();
        when(restTemplate.getForEntity(eq(url), eq(AccessTokenDTO.class)))
                .thenReturn(response);
        String emailContent = "update token failure email content";
        when(objectMapper.writeValueAsString(eq(userRegistrationDTO))).thenReturn(emailContent);

        fixture.processQueue();

        ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).send(anyString(), emailContentCaptor.capture());
//        ArgumentCaptor<UserRegistrationFailureDTO> failureDTOArgumentCaptor = ArgumentCaptor.forClass(UserRegistrationFailureDTO.class);
//        verify(logService, times(1)).logFailure(failureDTOArgumentCaptor.capture());
        assertEquals(0, messageQueue.size());
        assertEquals(emailContent, emailContentCaptor.getValue());
//        assertEquals(UserService.UNABLE_TO_UPDATE_ACCESS_TOKEN_MESSAGE, failureDTOArgumentCaptor.getValue().getErrorMessage());
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
        String url = UriComponentsBuilder.fromHttpUrl(CREATE_USER_URL).queryParam(UserService.ACCESS_TOKEN_KEY, accessToken).toUriString();
        when(restTemplate.postForEntity(eq(url), eq(userRegistrationDTO), eq(UserRegistrationResponseDTO.class)))
                .thenReturn(response);
        ArgumentCaptor<String> emailContentCaptor = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<UserRegistrationFailureDTO> failureLogCaptor = ArgumentCaptor.forClass(UserRegistrationFailureDTO.class);

        fixture.processQueue();

        assertEquals(0, messageQueue.size());
        verify(emailService, times(1)).send(anyString(), emailContentCaptor.capture());
//        verify(logService, times(1)).logFailure(failureLogCaptor.capture());
        assertTrue(emailContentCaptor.getValue().contains(userCreationErrorMessage));
//        assertEquals(userCreationErrorMessage, failureLogCaptor.getValue().getErrorMessage());
    }

    @Test
    public void testCheckBlocking() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        Integer threshold = 2;
        ReflectionTestUtils.setField(fixture, "blockingThreshold", threshold);
        messageQueue.add(dto);
        messageQueue.add(dto);
        messageQueue.add(dto);

        fixture.checkBlocking();

        verify(emailService, times(1)).send(anyString(), anyString());
    }
}
