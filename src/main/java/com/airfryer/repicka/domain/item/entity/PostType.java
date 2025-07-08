package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PostType
{
    RENTAL("RENTAL", "대여"),
    SALE("SALE", "판매");

    private final String code;
    private final String label;
}
