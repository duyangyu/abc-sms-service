package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.SmsRequestBO;

@Repository
public interface SmsRequestRepository extends PagingAndSortingRepository<SmsRequestBO, Long> {
}
