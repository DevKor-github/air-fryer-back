package com.airfryer.repicka.common.security.oauth2.dto;

public interface OAuth2Response {
    //제공자 (Ex. naver, google, ...)
    String getProvider();
    //제공자에서 발급해주는 아이디
    String getProviderId();
    //사용자 이메일
    String getEmail();
    //사용자 이름
    String getName();
}
