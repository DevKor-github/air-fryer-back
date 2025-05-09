package com.airfryer.repicka.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginMethod {
    KAKAO("LOGIN_KAKAO", "카카오 로그인"),
    GOOGLE("LOGIN_GOOGLE", "구글 로그인");

    private final String code;
    private final String label;
}