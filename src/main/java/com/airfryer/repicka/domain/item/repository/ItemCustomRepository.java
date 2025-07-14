package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.dto.SearchItemResult;
import com.airfryer.repicka.domain.item.dto.SearchItemReq;

public interface ItemCustomRepository {
    SearchItemResult findItemsByCondition(SearchItemReq condition);
}
