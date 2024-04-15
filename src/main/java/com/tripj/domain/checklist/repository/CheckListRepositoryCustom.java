package com.tripj.domain.checklist.repository;

import com.tripj.domain.checklist.model.dto.GetCheckListResponse;
import com.tripj.domain.checklist.model.dto.GetMyCheckListRequest;
import com.tripj.domain.checklist.model.dto.GetMyCheckListResponse;

import java.util.List;

public interface CheckListRepositoryCustom {

    List<GetCheckListResponse> getCheckList(Long itemCateId, Long userId, Long countryId);
    List<GetMyCheckListResponse> getMyCheckList(Long itemCateId, Long userId);

}
