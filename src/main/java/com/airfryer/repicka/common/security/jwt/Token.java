package com.airfryer.repicka.common.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

// JWT 토큰 종류
@AllArgsConstructor
@Getter
public enum Token
{
    ACCESS_TOKEN("accessToken", 60 * 60 * 24, false),          // 1일
    REFRESH_TOKEN("refreshToken", 60 * 60 * 24 * 30, true);   // 30일

    private final String name;
    private final int validTime;
    private final boolean isHttpOnly;
}
