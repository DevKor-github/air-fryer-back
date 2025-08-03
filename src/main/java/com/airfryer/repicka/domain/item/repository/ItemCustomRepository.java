package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.dto.req.SearchItemCountReq;
import com.airfryer.repicka.domain.item.dto.req.SearchItemReq;
import com.airfryer.repicka.domain.item.entity.Item;

import java.util.List;

public interface ItemCustomRepository {
    List<Item> findItemsByConditionWithoutCount(SearchItemReq condition);
    
    int countItemsByCondition(SearchItemCountReq condition);
}
