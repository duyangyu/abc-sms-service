package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.SmsRequestBO;

import java.util.Date;
import java.util.List;

@Repository
public interface SmsRequestRepository extends PagingAndSortingRepository<SmsRequestBO, Long> {

    List<SmsRequestBO> findAllByUpdateCountLessThanEqualAndUpdatedOnBefore(Integer maxCount, Date lastUpdatedOn);
}
