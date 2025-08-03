package com.airfryer.repicka.domain.item.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import com.airfryer.repicka.domain.item.dto.ItemPreviewDto;

@Getter
@Builder
public class SearchItemRes
{
    List<ItemPreviewDto> items;     // 제품 정보 리스트
    boolean hasNext;                // 다음 페이지 존재 여부
    Long cursorId;              // 마지막 아이템의 ID
    Integer cursorLike;    // 마지막 아이템의 좋아요 개수 (LIKE 정렬용)
    LocalDateTime cursorDate;   // 마지막 아이템의 repost_date (RECENT 정렬용)
}
