package org.theabconline.smsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.theabconline.smsservice.dto.UserRegistrationDTO;
import org.theabconline.smsservice.repository.FormRepository;

import java.io.IOException;
import java.sql.Ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class ParsingServiceTest {

    // TODO need get real data

    @Before
    public void setup() {
//        ReflectionTestUtils.setField(fixture, "defaultPath", "/data");
//        ReflectionTestUtils.setField(fixture, "appIdWidget", "appId");
//        ReflectionTestUtils.setField(fixture, "entryIdWidget", "entryId");
//        ReflectionTestUtils.setField(fixture, "dataIdWidget", "_id");
//        ReflectionTestUtils.setField(fixture, "mapper", new ObjectMapper());

    }

    @Test
    public void test() {
        assertTrue(true);
    }



}
