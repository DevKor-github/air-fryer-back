package com.airfryer.repicka.domain.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemOrder {
    RECENT("RECENT", "최신순"),
    LIKE("LIKE", "좋아요순"),
    RENTAL_FEE("RENTAL_FEE", "대여료순"),
    SALE_PRICE("SALE_PRICE", "판매값순");

    private final String code;
    private final String label;
}
