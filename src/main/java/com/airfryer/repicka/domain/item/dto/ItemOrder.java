package com.airfryer.repicka.domain.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemOrder {
    RECENT("RECENT", "최신순"),
    LIKE("LIKE", "좋아요순"),
    PRICE("CHEAP", "가격순");

    private final String code;
    private final String label;
}
