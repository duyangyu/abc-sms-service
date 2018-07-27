package org.theabconline.smsservice.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.entity.RawMessageBO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:/scripts/raw_message_int_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RawMessageBORepositoryIntTest {

    @Autowired
    private RawMessageRepository fixture;

    @Test
    public void testFindOne() {
        Long id = 1L;
        RawMessageBO bo = fixture.findOne(id);

        assertNotNull(bo);
        assertNotNull(bo.getCreatedOn());
        assertEquals(bo.getCreatedOn(), bo.getProcessedOn());
        assertEquals(id, bo.getId());
    }

}
