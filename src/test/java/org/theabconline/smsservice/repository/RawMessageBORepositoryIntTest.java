package org.theabconline.smsservice.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.entity.RawMessageBO;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SpringBootTest
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:/scripts/raw_message_int_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RawMessageBORepositoryIntTest {

    @Autowired
    private RawMessageRepository fixture;

    @Test
    public void testGetRawMessageBOSByIsProcessedFalse() {
        List<RawMessageBO> boList = fixture.getRawMessageBOSByIsProcessedFalse();

        assertEquals(1, boList.size());
        RawMessageBO bo = boList.get(0);
        assertEquals(Long.valueOf(1), bo.getId());
        assertFalse(bo.getProcessed());
    }

}
