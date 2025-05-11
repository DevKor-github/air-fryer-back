package com.airfryer.repicka.common.security.oauth2.dto;

import com.airfryer.repicka.domain.user.entity.LoginMethod;

public interface OAuth2Response
{
    // 제공자 (Ex. naver, google, ...)
    LoginMethod getProvider();

    // 제공자에서 발급해주는 아이디
    String getProviderId();

    // 사용자 이메일
    String getEmail();

    // 사용자 이름
    String getName();
}
