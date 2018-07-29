package org.theabconline.smsservice.repository;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.theabconline.smsservice.entity.RawMessageBO;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:/scripts/raw_message_int_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RawMessageBORepositoryIntTest {

    @Autowired
    private RawMessageRepository fixture;

    @Test
    public void testSave() {
        RawMessageBO bo = new RawMessageBO();
        bo.setMessage("message");
        bo.setProcessed(false);

        fixture.save(bo);

        List<RawMessageBO> boList = Lists.newArrayList(fixture.findAll());
        assertEquals(3, boList.size());
        assertNotNull(bo.getCreatedOn());
        assertNull(bo.getProcessedOn());
    }

    @Test
    public void testSaveEmptyList() {
        List<RawMessageBO> emptyList = Lists.newArrayList();
        fixture.save(emptyList);

        assertEquals(2, fixture.count());
    }

    @Test
    public void testGetRawMessageBOSByIsProcessedFalse() {
        List<RawMessageBO> boList = fixture.getRawMessageBOSByIsProcessedFalse();

        assertEquals(1, boList.size());
        RawMessageBO bo = boList.get(0);
        assertEquals(Long.valueOf(1), bo.getId());
        assertFalse(bo.getProcessed());
    }

}
