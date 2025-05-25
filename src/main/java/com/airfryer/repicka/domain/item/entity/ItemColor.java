package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemColor
{
    CRIMSON("CRIMSON", "크림슨"),
    WHITE("CRIMSON", "하양"),
    BLACK("BLACK", "검정"),
    IVORY("IVORY", "아이보리"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String label;
}
