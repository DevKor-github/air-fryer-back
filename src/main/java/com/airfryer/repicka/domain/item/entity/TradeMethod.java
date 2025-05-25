package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeMethod
{
    DIRECT("DIRECT", "직거래"),
    PARCEL("PARCEL", "택배거래"),
    DIRECT_AND_PARCEL("DIRECT_AND_PARCEL", "직거래 및 택배거래");

    private final String code;
    private final String label;
}
