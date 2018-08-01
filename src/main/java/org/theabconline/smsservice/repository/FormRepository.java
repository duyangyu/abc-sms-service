package org.theabconline.smsservice.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.theabconline.smsservice.entity.FormBO;

import java.util.List;

@Repository
public interface FormRepository extends PagingAndSortingRepository<FormBO, Long> {

    List<FormBO> findAllByAppIdAndEntryId(String appId, String entryId);
}
