package com.airfryer.repicka.domain.user.entity.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Gender {
    MALE("M", "남성"),
    FEMALE("F", "여성");

    private final String code;
    private final String label;
}
