package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.theabconline.smsservice.entity.RawMessageBO;

public interface RawMessageRepository extends PagingAndSortingRepository<RawMessageBO, Long> {
}
