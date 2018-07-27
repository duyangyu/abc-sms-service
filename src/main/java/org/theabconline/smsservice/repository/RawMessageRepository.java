package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.theabconline.smsservice.entity.RawMessageBO;

import java.util.List;

public interface RawMessageRepository extends PagingAndSortingRepository<RawMessageBO, Long> {

    List<RawMessageBO> getRawMessageBOSByIsProcessedFalse();
}
