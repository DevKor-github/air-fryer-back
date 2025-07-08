package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.item.entity.PostType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchItemReq
{
    // 페이지 번호 (0부터 시작)
    private int page = 0;

    // 검색 키워드
    private String keyword;

    // 제품 타입
    private ProductType[] productTypes;

    // 게시글 타입
    private PostType[] postTypes;

    // 제품 사이즈
    private ItemSize[] sizes;

    // 제품 색상
    private ItemColor[] colors;

    // 원하는 거래 날짜
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;

    // 제품 정렬 순서
    private ItemOrder itemOrder = ItemOrder.RECENT;
}
