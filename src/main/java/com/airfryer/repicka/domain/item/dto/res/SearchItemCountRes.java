package com.airfryer.repicka.domain.item.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchItemCountRes {
    private Integer totalCount; // 검색 결과 개수
}