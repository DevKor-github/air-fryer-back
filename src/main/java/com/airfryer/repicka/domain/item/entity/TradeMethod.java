package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeMethod
{
    DIRECT("M", "직거래"),
    PARCEL("S", "택배거래");

    private final String code;
    private final String label;
}
