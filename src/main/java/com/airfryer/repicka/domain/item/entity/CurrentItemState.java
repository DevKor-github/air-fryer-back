package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrentItemState
{
    AVAILABLE("AVAILABLE", "구매 가능"),
    SALE_RESERVED("SALE_RESERVED", "판매 예정"),
    RENTED("RENTED", "대여중"),
    SOLD_OUT("SOLD_OUT", "판매됨");

    private final String code;
    private final String label;
}
