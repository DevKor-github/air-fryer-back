package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateItemReq {
    @NotNull(message = "타입을 입력해주세요.")
    private ProductType[] productTypes = new ProductType[2]; // 제품 타입

    @NotNull(message = "사이즈를 입력해주세요.")
    private ItemSize size; // 제품 사이즈

    private ItemColor color; // 제품 색상

    private ItemQuality quality; // 제품 품질

    @Size(max = 255, message = "제목은 최대 255자까지 입력할 수 있습니다.")
    @NotBlank(message = "제목을 입력해주세요.")
    private String title; // 제목

    private String description; // 설명

    @Size(max = 255, message = "장소는 최대 255자까지 입력할 수 있습니다.")
    private String location; // 장소

    private TradeMethod tradeMethod; // 거래 방식

    @NotNull(message = "가격 제시 가능 여부를 입력해주세요.")
    private Boolean canDeal; // 가격 제시 여부
}
