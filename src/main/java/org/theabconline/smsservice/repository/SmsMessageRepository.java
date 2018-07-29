package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.SmsMessageBO;

@Repository
public interface SmsMessageRepository extends PagingAndSortingRepository<SmsMessageBO, Long> {
}
