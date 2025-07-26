package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;
import lombok.Getter;

@Getter
public class CreateItemReq
{
    @NotNull(message = "제품 타입을 입력해주세요.")
    private ProductType[] productTypes = new ProductType[2];

    @NotNull(message = "거래 타입을 입력해주세요.")
    private TransactionType[] transactionTypes = new TransactionType[2];

    @Size(max = 64, message = "제목은 최대 64자까지 입력할 수 있습니다.")
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;   // 제목

    @Size(max = 1024, message = "설명은 최대 1024자까지 입력할 수 있습니다.")
    private String description; // 설명

    private ItemColor color;    // 제품 색상

    @NotNull(message = "사이즈를 입력해주세요.")
    private ItemSize size;

    private ItemQuality quality;    // 제품 품질

    @Range(min = 0, max = 999999, message = "대여료는 0원 이상 999,999원 이하이어야 합니다.")
    private int rentalFee;

    @Range(min = 0, max = 999999, message = "판매값은 0원 이상 999,999원 이하이어야 합니다.")
    private int salePrice;

    @Range(min = 0, max = 999999, message = "보증금은 0원 이상 999,999원 이하이어야 합니다.")
    private int deposit = 0;

    @Size(max = 100, message = "장소는 최대 100자까지 입력할 수 있습니다.")
    private String location;

    @NotNull(message = "거래 방식을 입력해주세요.")
    private TradeMethod[] tradeMethods = new TradeMethod[2];

    @NotNull(message = "가격 제시 가능 여부를 입력해주세요.")
    private Boolean canDeal;

    @NotNull(message = "이미지 키를 첨부해주세요.")
    @Size(min = 1, max = 6, message = "이미지는 최소 1개 이상, 최대 6개 이하이어야 합니다.")
    private String[] images = new String[6];
}
