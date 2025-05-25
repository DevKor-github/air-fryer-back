package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemSize
{
    XXL("XXL", "XXL"),
    XL("XL", "XL"),
    L("L", "L"),
    M("M", "M"),
    S("S", "S"),
    XS("XS", "XS");

    private final String code;
    private final String label;
}
