package com.airfryer.repicka.domain.item.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ItemQuality
{
    BEST("CRIMSON", "최상"),
    HIGH("CRIMSON", "상"),
    MIDDLE("CRIMSON", "중"),
    LOW("CRIMSON", "하");

    private final String code;
    private final String label;
}
