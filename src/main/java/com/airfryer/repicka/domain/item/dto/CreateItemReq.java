package com.airfryer.repicka.domain.item.dto;

import com.airfryer.repicka.domain.item.entity.*;
import lombok.Data;

@Data
public class CreateItemReq {
    private ProductType[] productTypes = new ProductType[2];
    private ItemSize size;
    private String title;
    private String description;
    private ItemColor color;
    private ItemQuality quality;
    private String location;
    private TradeMethod tradeMethod;
    private Boolean canDeal;
    private CurrentItemState state;
}
