package com.airfryer.repicka.domain.user.entity.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginMethod {
    GOOGLE("google", "구글 로그인"),
    KAKAO("kakao", "카카오 로그인");

    private final String provider;
    private final String label;
}