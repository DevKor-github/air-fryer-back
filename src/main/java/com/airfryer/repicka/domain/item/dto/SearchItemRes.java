package com.airfryer.repicka.domain.item.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchItemRes
{
    List<ItemPreviewDto> items;     // 제품 정보 리스트
    Integer totalCount;                 // 결과 개수
}
