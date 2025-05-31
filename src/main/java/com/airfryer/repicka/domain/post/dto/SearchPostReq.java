package com.airfryer.repicka.domain.post.dto;

import com.airfryer.repicka.domain.item.entity.*;
import lombok.Getter;

@Getter
public class SearchPostReq {
    private ProductType productType; // 제품 타입

    private ItemSize size; // 제품 사이즈

    private ItemColor color; // 제품 색상

    private ItemQuality quality; // 제품 품질

    private TradeMethod tradeMethod; // 거래 방식

    private PostOrder postOrder = PostOrder.RECENT; // 게시글 순서
}
