package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.SmsMessageBO;

import java.util.List;

@Repository
public interface SmsMessageRepository extends PagingAndSortingRepository<SmsMessageBO, Long> {

    List<SmsMessageBO> getAllByRecordIdAndIsSent(Long recordId, Boolean isSent);

    Integer countAllBySmsRequestIdAndIsSent(Long smsRequestId, Boolean isSent);

    List<SmsMessageBO> getAllBySmsRequestIdAndIsSent(Long smsRequestId, Boolean isSent);

    List<SmsMessageBO> getAllByBizIdAndPhoneNumber(String bizId, String phoneNumber);
}
