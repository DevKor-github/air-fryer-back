package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateItemReq
{
    @NotNull(message = "제품 타입을 입력해주세요.")
    private ProductType[] productTypes = new ProductType[2];

    @NotNull(message = "거래 타입을 입력해주세요.")
    private TransactionType[] transactionTypes = new TransactionType[2];

    @Size(max = 255, message = "제목은 최대 255자까지 입력할 수 있습니다.")
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String description; // 설명

    private ItemColor color;    // 제품 색상

    @NotNull(message = "사이즈를 입력해주세요.")
    private ItemSize size;

    private ItemQuality quality;    // 제품 품질

    @Min(value = 0, message = "대여료는 0원 이상이어야 합니다.")
    private int rentalFee;

    @Min(value = 0, message = "판매값은 0원 이상이어야 합니다.")
    private int salePrice;

    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private int deposit = 0;

    @Size(max = 255, message = "장소는 최대 255자까지 입력할 수 있습니다.")
    private String location;

    @NotNull(message = "거래 방식을 입력해주세요.")
    private TradeMethod[] tradeMethods = new TradeMethod[2];

    @NotNull(message = "가격 제시 가능 여부를 입력해주세요.")
    private Boolean canDeal;

    @NotNull(message = "이미지 키를 첨부해주세요.")
    @Size(min = 1, max = 6, message = "이미지는 최소 1개 이상, 최대 6개 이하이어야 합니다.")
    private String[] images = new String[6];
}
