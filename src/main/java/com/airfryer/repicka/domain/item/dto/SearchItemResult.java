package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.Item;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchItemResult
{
    List<Item> items;   // 제품 리스트
    Integer totalCount;     // 결과 개수
}
