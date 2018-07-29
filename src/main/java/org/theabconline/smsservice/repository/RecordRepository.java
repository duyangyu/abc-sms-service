package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.RecordBO;

@Repository
public interface RecordRepository extends PagingAndSortingRepository<RecordBO, Long> {
}
