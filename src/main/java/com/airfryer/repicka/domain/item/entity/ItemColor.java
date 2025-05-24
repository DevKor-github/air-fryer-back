package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemColor
{
    CRIMSON("CRIMSON", "크림슨"),
    WHITE("CRIMSON", "하양"),
    BLACK("CRIMSON", "검정"),
    IVORY("CRIMSON", "아이보리"),
    OTHER("CRIMSON", "기타");

    private final String code;
    private final String label;
}
