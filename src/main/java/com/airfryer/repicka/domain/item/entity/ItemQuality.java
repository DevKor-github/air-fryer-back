package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemQuality
{
    BEST("BEST", "최상"),
    HIGH("HIGH", "상"),
    MIDDLE("MIDDLE", "중"),
    LOW("LOW", "하");

    private final String code;
    private final String label;
}
