package com.airfryer.repicka.common.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;

// JWT 토큰 종류
@AllArgsConstructor
@Getter
public enum Token
{
    ACCESS_TOKEN("accessToken", 60 * 60),               // 1시간
    REFRESH_TOKEN("refreshToken", 60 * 60 * 24 * 30);   // 30일

    private final String name;
    private final int validTime;
}
