package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SearchPostReq {
    private String keyword; // 검색 키워드

    private ProductType productType; // 제품 타입

    private ItemSize size; // 제품 사이즈

    private ItemColor color; // 제품 색상

    private ItemQuality quality; // 제품 품질

    private TradeMethod tradeMethod; // 거래 방식

    private LocalDateTime rentalDate; // 원하는 대여 날짜

    private PostOrder postOrder = PostOrder.RECENT; // 게시글 순서
}
