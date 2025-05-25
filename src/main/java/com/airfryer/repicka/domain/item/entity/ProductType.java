package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductType
{
    HOCKEY("HOCKEY", "하키"),
    SOCCER("SOCCER", "축구"),
    BASKETBALL("BASKETBALL", "농구"),
    BASEBALL("BASEBALL", "야구"),
    VARSITY_JACKET("VARSITY_JACKET", "과잠"),
    ACCESSORY("ACCESSORY", "악세사리"),
    SELF_MADE("SELF_MADE", "자체 제작"),
    VINTAGE("VINTAGE", "빈티지"),
    REFORM("REFORM", "리폼"),
    OTHER("OTHER", "기타");

    private final String code;
    private final String label;
}
