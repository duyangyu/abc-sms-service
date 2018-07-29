package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.RawMessageBO;

import java.util.List;

@Repository
public interface RawMessageRepository extends PagingAndSortingRepository<RawMessageBO, Long> {

    List<RawMessageBO> getRawMessageBOSByIsProcessedFalse();
}
