package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.RecordBO;

import java.util.Date;
import java.util.List;

@Repository
public interface RecordRepository extends PagingAndSortingRepository<RecordBO, Long> {

    List<RecordBO> findAllByUpdateCountLessThanEqualAndUpdatedOnBefore(Integer maxCount, Date lastUpdatedOn);
}
