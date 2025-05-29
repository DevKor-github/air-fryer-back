package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import com.airfryer.repicka.domain.post.entity.PostType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BaseItemDto {
    @Builder.Default
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입
    private String title; // 게시글 제목
    private String description; // 게시글 내용
    private ItemSize size; // 사이즈
    private ItemColor color; // 색상
    private ItemQuality quality; // 품질
    private String location; // 거래 장소
    private TradeMethod tradeMethod; // 거래 방법
    private Boolean canDeal; // 가격 제시 가능 여부
    private CurrentItemState state; // 현재 상품 대여, 예약, 판매 상태

    public static BaseItemDto from(Item item) {
        return BaseItemDto.builder()
                .productTypes(item.getProductTypes())
                .title(item.getTitle())
                .description(item.getDescription())
                .size(item.getSize())
                .color(item.getColor())
                .quality(item.getQuality())
                .location(item.getLocation())
                .tradeMethod(item.getTradeMethod())
                .canDeal(item.getCanDeal())
                .state(item.getState())
                .build();
    }
}
