package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateItemReq {
    @NotNull(message = "타입을 입력해주세요.")
    private ProductType[] productTypes = new ProductType[2];

    @NotNull(message = "사이즈를 입력해주세요.")
    private ItemSize size;

    @Size(max = 255, message = "제목은 최대 255자까지 입력할 수 있습니다.")
    @NotBlank(message = "제목을 입력해주세요.")
    private String title;

    private String description;

    private ItemColor color;

    private ItemQuality quality;

    @Size(max = 255, message = "장소는 최대 255자까지 입력할 수 있습니다.")
    private String location;

    private TradeMethod tradeMethod;

    @NotNull(message = "가격 제시 가능 여부를 입력해주세요.")
    private Boolean canDeal;

    @NotNull(message = "제품 상태를 입력해주세요.")
    private CurrentItemState state;
}
