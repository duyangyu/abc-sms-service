package org.theabconline.smsservice.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.theabconline.smsservice.entity.RecordBO;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SpringBootTest
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:/scripts/record_int_test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class RecordRepositoryIntTest {

    @Autowired
    private RecordRepository fixture;

    @Test
    public void testSave() {
        RecordBO recordBO = new RecordBO();

        fixture.save(recordBO);

        assertNotNull(recordBO.getId());
        assertNotNull(recordBO.getCreatedOn());
        assertNotNull(recordBO.getUpdatedOn());
        assertEquals(recordBO.getUpdatedOn(), recordBO.getCreatedOn());
        assertEquals(Integer.valueOf(0), recordBO.getUpdateCount());
    }

    @Test
    public void testFindAllByUpdateCountLessThanEqualAndUpdatedOnBefore() {
        Integer maxCount = 1;
        Date lastUpdatedOn = new Date();

        List<RecordBO> recordBOList = fixture.findAllByUpdateCountLessThanEqualAndUpdatedOnBefore(maxCount, lastUpdatedOn);

        assertEquals(1, recordBOList.size());
        RecordBO recordBO = recordBOList.get(0);
        assertEquals(Long.valueOf(1), recordBO.getId());
        assertEquals(Integer.valueOf(1), recordBO.getUpdateCount());
    }
}
