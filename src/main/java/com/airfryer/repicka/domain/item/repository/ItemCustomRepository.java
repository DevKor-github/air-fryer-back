package com.airfryer.repicka.domain.item.repository;

import com.airfryer.repicka.domain.item.entity.Item;
import com.airfryer.repicka.domain.item.dto.SearchItemReq;

import java.util.List;

public interface ItemCustomRepository {
    List<Item> findItemsByCondition(SearchItemReq condition);
}
