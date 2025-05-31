package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CurrentItemState
{
    AVAILABLE("AVAILABLE", "가능"),
    RENTAL_RESERVED("RENTAL_RESERVED", "대여 예정"),
    SALE_RESERVED("SALE_RESERVED", "판매 예정"),
    RENTED("RENTED", "대여중"),
    SOLD_OUT("SOLD_OUT", "판매됨");

    private final String code;
    private final String label;
}
